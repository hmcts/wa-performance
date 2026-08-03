package scenarios.taskmessages

import io.gatling.core.Predef._

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.DriverManager
import java.time.Instant
import java.util.{Base64, UUID}
import java.util.concurrent.atomic.{AtomicLong, LongAdder}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scala.concurrent.duration._
import scala.util.Try

/** Settings deliberately read from environment/properties: no database or Service Bus secret is stored in source. */
object TaskMessageLatencyConfig {
  private def setting(name: String, default: String, environmentNames: String*): String =
    Option(System.getProperty(name)).filter(_.nonEmpty)
      .orElse(environmentNames.iterator.map(System.getenv).find(value => value != null && value.nonEmpty))
      .orElse(Option.when(environmentNames.isEmpty)(System.getenv(name.toUpperCase.replace('.', '_'))).filter(value => value != null && value.nonEmpty))
      .getOrElse(default)

  val connectionString: String = setting("serviceBus.connectionString", "", "AZURE_SERVICE_BUS_CONNECTION_STRING")
  val topic: String = setting("serviceBus.topic", "wa-ccd-case-events-sub-perftest", "AZURE_SERVICE_BUS_TOPIC_NAME")
  val ratePerMinute: Double = setting("messageRatePerMinute", "167", "MESSAGE_RATE_PER_MINUTE").toDouble
  val duration: FiniteDuration = setting("messageDurationMinutes", "525600", "MESSAGE_DURATION_MINUTES").toLong.minutes
  val pollInterval: FiniteDuration = setting("taskPollIntervalSeconds", "2", "TASK_POLL_INTERVAL_SECONDS").toLong.seconds
  val maxPolls: Int = setting("taskMaxPolls", "60", "TASK_MAX_POLLS").toInt

  val jurisdictionId: String = setting("jurisdictionId", "WA")
  val caseTypeId: String = setting("caseTypeId", "WaCaseType")
  val eventId: String = setting("eventId", "dummySubmitAppeal")
  val userId: String = setting("userId", "system")
  val messageAuthor: String = setting("messageAuthor", "end-to-end-performance-test")
  val roleCategory: String = setting("roleCategory", "JUDICIAL", "ROLE_CATEGORY")
  // Valid WA pairing observed in cft_task_db.tasks for JUDICIAL tasks.
  val workType: String = setting("workType", "access_requests", "WORK_TYPE")

  val dbHost: String = setting("taskDbHost", "cft-task-postgres-db-flexible-perftest.postgres.database.azure.com", "TASK_DB_HOST")
  val dbName: String = setting("taskDbName", "cft_task_db", "TASK_DB_NAME")
  val dbUser: String = setting("taskDbUser", "", "PERFTEST_TASK_DB_USER", "TASK_DB_USER")
  val dbPassword: String = setting("taskDbPassword", "", "PERFTEST_TASK_DB_PASSWORD", "TASK_DB_PASSWORD")
  val dbUrl: String = setting("taskDbUrl", s"jdbc:postgresql://$dbHost:5432/$dbName?sslmode=require", "TASK_DB_URL")
  // PostgreSQL places the task table in this schema; it is not in the default public search path.
  val dbSchema: String = setting("taskDbSchema", "cft_task_db", "TASK_DB_SCHEMA")
  // Must return one non-empty column named `case_id`. The most recent case avoids a costly full-table random sort.
  val validCaseIdSql: String = setting("taskDbValidCaseIdSql", s"SELECT case_id FROM $dbSchema.tasks WHERE case_id IS NOT NULL ORDER BY created DESC LIMIT 1", "TASK_DB_VALID_CASE_ID_SQL")
  // Must return one timestamp column named `created`; parameters are case_id and the publish timestamp.
  val taskLookupSql: String = setting("taskDbLookupSql", s"SELECT created FROM $dbSchema.tasks WHERE case_id = ? AND created >= ? ORDER BY created ASC LIMIT 1", "TASK_DB_LOOKUP_SQL")

  def validate(): Unit = {
    require(connectionString.nonEmpty, "AZURE_SERVICE_BUS_CONNECTION_STRING (or -DserviceBus.connectionString) is required")
    require(dbUser.nonEmpty, "TASK_DB_USER (or -DtaskDbUser) is required")
    require(dbPassword.nonEmpty, "TASK_DB_PASSWORD (or -DtaskDbPassword) is required")
    require(dbSchema.matches("[A-Za-z_][A-Za-z0-9_]*"), "taskDbSchema must be a PostgreSQL identifier")
    require(ratePerMinute > 0, "messageRatePerMinute must be greater than zero")
    require(duration.length > 0 && maxPolls > 0 && pollInterval.length > 0, "message duration and task polling configuration must be positive")
  }
}

object ServiceBusSasToken {
  private def parts(connectionString: String): Map[String, String] =
    connectionString.split(';').iterator.flatMap { item =>
      item.split("=", 2) match { case Array(key, value) => Some(key -> value); case _ => None }
    }.toMap

  val namespace: String = {
    val endpoint = parts(TaskMessageLatencyConfig.connectionString).getOrElse("Endpoint", "")
    endpoint.stripPrefix("sb://").stripSuffix("/").split('.').headOption.getOrElse("")
  }

  def value(): String = {
    val config = TaskMessageLatencyConfig
    val values = parts(config.connectionString)
    val keyName = values.getOrElse("SharedAccessKeyName", throw new IllegalArgumentException("Service Bus connection string has no SharedAccessKeyName"))
    val key = values.getOrElse("SharedAccessKey", throw new IllegalArgumentException("Service Bus connection string has no SharedAccessKey"))
    val resource = s"https://$namespace.servicebus.windows.net/${config.topic}"
    val encodedResource = URLEncoder.encode(resource.toLowerCase, StandardCharsets.UTF_8)
    val expiry = Instant.now.plusSeconds(7200).getEpochSecond.toString
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
    val signature = Base64.getEncoder.encodeToString(mac.doFinal(s"$encodedResource\n$expiry".getBytes(StandardCharsets.UTF_8)))
    s"SharedAccessSignature sr=$encodedResource&sig=${URLEncoder.encode(signature, StandardCharsets.UTF_8)}&se=$expiry&skn=$keyName"
  }
}

object TaskDatabase {
  Class.forName("org.postgresql.Driver")

  private def withConnection[A](operation: java.sql.Connection => A): A = {
    val config = TaskMessageLatencyConfig
    val connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword)
    try operation(connection)
    finally connection.close()
  }

  /** Obtains a case id already known to task-management, rather than synthesising an invalid CCD id. */
  def validCaseId(): Option[String] = withConnection { connection =>
    val statement = connection.prepareStatement(TaskMessageLatencyConfig.validCaseIdSql)
    try {
      val result = statement.executeQuery()
      try Option.when(result.next())(result.getString("case_id")).filter(_.nonEmpty)
      finally result.close()
    } finally statement.close()
  }

  /** Returns a task created after publication, if the consumer has committed it yet. */
  def createdFor(caseId: String, publishedAt: Long): Option[Long] = withConnection { connection =>
    val config = TaskMessageLatencyConfig
      val statement = connection.prepareStatement(config.taskLookupSql)
      try {
        statement.setString(1, caseId)
        statement.setTimestamp(2, new java.sql.Timestamp(publishedAt))
        val result = statement.executeQuery()
        try Option.when(result.next())(result.getTimestamp("created").toInstant.toEpochMilli)
        finally result.close()
      } finally statement.close()
  }
}

object TaskLatencyMetrics {
  private val successful = new LongAdder
  private val timedOut = new LongAdder
  private val totalMillis = new AtomicLong(0)

  def record(latencyMillis: Long): Unit = { successful.increment(); totalMillis.addAndGet(latencyMillis) }
  def timeout(): Unit = timedOut.increment()

  def summary: String = {
    val count = successful.sum()
    val average = if (count == 0) 0 else totalMillis.get().toDouble / count
    f"Task propagation latency: successful=$count, timedOut=${timedOut.sum()}, average=${average}%.1f ms"
  }
}

object TaskMessageLatencyJourney {
  private def escapeJson(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")

  val prepareMessage = exec { session =>
    Try(TaskDatabase.validCaseId()) match {
      case scala.util.Success(Some(caseId)) =>
        val eventInstanceId = UUID.randomUUID().toString
        val messageId = UUID.randomUUID().toString
        val publishedAt = System.currentTimeMillis()
        val config = TaskMessageLatencyConfig
        val body = s"""{"EventInstanceId":"$eventInstanceId","EventTimeStamp":"${Instant.ofEpochMilli(publishedAt)}","CaseId":"$caseId","JurisdictionId":"${escapeJson(config.jurisdictionId)}","CaseTypeId":"${escapeJson(config.caseTypeId)}","EventId":"${escapeJson(config.eventId)}","PreviousStateId":null,"NewStateId":null,"UserId":"${escapeJson(config.userId)}","AdditionalData":{"Data":{"roleCategory":"${escapeJson(config.roleCategory)}","workType":"${escapeJson(config.workType)}"},"Definition":{}},"MessageProperties":{"batchId":"gatling-latency"},"HoldUntil":null}"""
        // Refresh the SAS token for every message so a long-running test does not expire after two hours.
        session.setAll("caseId" -> caseId, "messageId" -> messageId, "publishedAt" -> publishedAt, "messageBody" -> body, "sasToken" -> ServiceBusSasToken.value(), "taskCreated" -> false, "pollCount" -> 0)
      case scala.util.Success(None) => session.markAsFailed.set("caseIdLookupError", "no_case_id")
      case scala.util.Failure(error) => session.markAsFailed.set("caseIdLookupError", error.getClass.getSimpleName)
    }
  }

  val findCreatedTask = exec { session =>
    val nextPoll = session("pollCount").as[Int] + 1
    Try(TaskDatabase.createdFor(session("caseId").as[String], session("publishedAt").as[Long])) match {
      case scala.util.Success(Some(createdAt)) => session.setAll("taskCreated" -> true, "createdAt" -> createdAt, "pollCount" -> nextPoll)
      case scala.util.Success(None) => session.set("pollCount", nextPoll)
      case scala.util.Failure(error) => session.markAsFailed.set("taskLookupError", error.getClass.getSimpleName)
    }
  }

  val finishLatency = exec { session =>
    if (session("taskCreated").as[Boolean]) {
      val latency = session("createdAt").as[Long] - session("publishedAt").as[Long]
      TaskLatencyMetrics.record(latency)
      session.set("taskPropagationLatencyMs", latency)
    } else {
      TaskLatencyMetrics.timeout()
      session.markAsFailed
    }
  }
}
