package scenarios.taskmessages

import com.azure.messaging.servicebus.{ServiceBusClientBuilder, ServiceBusMessage, ServiceBusSenderClient}
import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.Predef._
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.sql.DriverManager
import java.time.Instant
import java.util.{Locale, UUID}
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.{AtomicLong, LongAdder}
import scala.jdk.CollectionConverters._
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
  private val legacyRatePerMinute = setting("messageRatePerMinute", "167", "MESSAGE_RATE_PER_MINUTE").toDouble
  val eventsPerHour: Double = setting("eventsPerHour", (legacyRatePerMinute * 60).toString, "EVENTS_PER_HOUR").toDouble
  val duration: FiniteDuration = setting("messageDurationMinutes", "60", "MESSAGE_DURATION_MINUTES").toLong.minutes
  val pollInterval: FiniteDuration = setting("taskPollIntervalSeconds", "2", "TASK_POLL_INTERVAL_SECONDS").toLong.seconds
  val maxPolls: Int = setting("taskMaxPolls", "60", "TASK_MAX_POLLS").toInt
  val debug: Boolean = System.getProperty("debug", "off").equalsIgnoreCase("on")
  val debugEventCount: Int = setting("debugEventCount", "1", "DEBUG_EVENT_COUNT").toInt

  val jurisdictionId: String = setting("jurisdictionId", "WA")
  val caseTypeId: String = setting("caseTypeId", "WaCaseType")
  val eventId: String = setting("eventId", "endToEndTask")
  val newStateId: String = setting("newStateId", "TODO", "NEW_STATE_ID")
  val expectedTaskType: String = setting("expectedTaskType", "endToEndTask", "EXPECTED_TASK_TYPE")
  val userId: String = setting("userId", "system")
  val messageAuthor: String = setting("messageAuthor", "end-to-end-performance-test", "AZURE_SERVICE_BUS_MESSAGE_AUTHOR")
  val messageContextPrefix: String = setting("messageContextPrefix", "wa-ft-", "AZURE_SERVICE_BUS_MESSAGE_CONTEXT_PREFIX")
  val roleCategory: String = setting("roleCategory", "JUDICIAL", "ROLE_CATEGORY")
  val workType: String = setting("workType", "access_requests", "WORK_TYPE")

  val dbHost: String = setting("taskDbHost", "cft-task-postgres-db-flexible-perftest.postgres.database.azure.com", "TASK_DB_HOST")
  val dbName: String = setting("taskDbName", "cft_task_db", "TASK_DB_NAME")
  val dbUser: String = setting("taskDbUser", "", "PERFTEST_TASK_DB_USER", "TASK_DB_USER")
  val dbPassword: String = setting("taskDbPassword", "", "PERFTEST_TASK_DB_PASSWORD", "TASK_DB_PASSWORD")
  val dbUrl: String = setting("taskDbUrl", s"jdbc:postgresql://$dbHost:5432/$dbName?sslmode=require", "TASK_DB_URL")
  // PostgreSQL places the task table in this schema; it is not in the default public search path.
  val dbSchema: String = setting("taskDbSchema", "cft_task_db", "TASK_DB_SCHEMA")
  private val maximumInFlight = if (debug) debugEventCount else math.ceil(eventsPerHour * (maxPolls * pollInterval.toMillis / 1000d) / 3600d).toInt
  val casePoolSize: Int = setting("casePoolSize", math.max(1, maximumInFlight).toString, "CASE_POOL_SIZE").toInt
  // Parameters are jurisdiction, case type and limit. endToEndTask cases are created against CCD by the WA tests.
  // Each in-flight event leases a different case id.
  private val defaultValidCaseIdsSql =
    s"SELECT case_id FROM $dbSchema.tasks WHERE case_id IS NOT NULL AND jurisdiction = ? AND case_type_id = ? AND task_type = 'endToEndTask' GROUP BY case_id ORDER BY MAX(created) DESC LIMIT ?"
  val validCaseIdsSql: String = setting("taskDbValidCaseIdsSql",
    defaultValidCaseIdsSql,
    "TASK_DB_VALID_CASE_IDS_SQL")
  // The indexed case id keeps polling cheap; the idempotency key uniquely identifies the exact task.
  val taskLookupSql: String = setting("taskDbLookupSql",
    s"SELECT task_id, (EXTRACT(EPOCH FROM created AT TIME ZONE 'UTC') * 1000)::bigint AS created_epoch_ms FROM $dbSchema.tasks WHERE case_id = ? AND additional_properties ->> 'idempotencyKey' = ? LIMIT 1",
    "TASK_DB_LOOKUP_SQL")

  def validate(): Unit = {
    require(connectionString.nonEmpty, "AZURE_SERVICE_BUS_CONNECTION_STRING (or -DserviceBus.connectionString) is required")
    require(dbUser.nonEmpty, "TASK_DB_USER (or -DtaskDbUser) is required")
    require(dbPassword.nonEmpty, "TASK_DB_PASSWORD (or -DtaskDbPassword) is required")
    require(dbSchema.matches("[A-Za-z_][A-Za-z0-9_]*"), "taskDbSchema must be a PostgreSQL identifier")
    require(eventsPerHour > 0, "eventsPerHour must be greater than zero")
    require(debugEventCount > 0, "debugEventCount must be greater than zero")
    require(casePoolSize > 0, "casePoolSize must be greater than zero")
    require(duration.length > 0 && maxPolls > 0 && pollInterval.length > 0, "message duration and task polling configuration must be positive")
  }
}

object ServiceBusPublisher {
  private lazy val sender: ServiceBusSenderClient = new ServiceBusClientBuilder()
    .connectionString(TaskMessageLatencyConfig.connectionString)
    .sender()
    .topicName(TaskMessageLatencyConfig.topic)
    .buildClient()

  def initialize(): Unit = sender

  def send(body: String, caseId: String, messageId: String): Unit = {
    val config = TaskMessageLatencyConfig
    val message = new ServiceBusMessage(body)
    message.setMessageId(messageId)
    message.setSessionId(caseId)
    message.setContentType("application/json")
    message.getApplicationProperties.put("jurisdiction_id", config.jurisdictionId)
    message.getApplicationProperties.put("case_type_id", config.caseTypeId)
    message.getApplicationProperties.put("case_id", caseId)
    message.getApplicationProperties.put("event_id", config.eventId)
    message.getApplicationProperties.put("message_author", config.messageAuthor)
    message.getApplicationProperties.put("message_context", s"${config.messageContextPrefix}$caseId")
    sender.sendMessage(message)
  }

  def close(): Unit = sender.close()
}

final class ServiceBusPublishActionBuilder extends ActionBuilder {
  override def build(context: ScenarioContext, next: Action): Action =
    new ServiceBusPublishAction(context.coreComponents.statsEngine, next)
}

private final class ServiceBusPublishAction(
  override val statsEngine: StatsEngine,
  override val next: Action
) extends ChainableAction {
  override val name: String = "PublishCaseEvent"

  override def execute(session: Session): Unit = {
    val caseId = session("caseId").as[String]
    val messageId = session("messageId").as[String]
    val startedAt = System.currentTimeMillis()
    Try(ServiceBusPublisher.send(session("messageBody").as[String], caseId, messageId)) match {
      case scala.util.Success(_) =>
        val completedAt = System.currentTimeMillis()
        TaskLatencyMetrics.publicationAccepted()
        if (TaskMessageLatencyConfig.debug) {
          println(s"Service Bus SDK accepted message: caseId=$caseId, messageId=$messageId, sendDurationMs=${completedAt - startedAt}")
        }
        next ! session.setAll("publishAccepted" -> true, "publishCompletedAt" -> completedAt)
      case scala.util.Failure(error) =>
        val completedAt = System.currentTimeMillis()
        val message = s"${error.getClass.getSimpleName}: ${error.getMessage}"
        TaskLatencyMetrics.publicationRejected()
        CaseIdPool.release(caseId)
        println(s"Service Bus SDK rejected message: caseId=$caseId, messageId=$messageId, error=$message")
        next ! session.setAll("publishAccepted" -> false, "publishCompletedAt" -> completedAt, "publishError" -> message).markAsFailed
    }
  }
}

/** Records one Gatling request from message publication until the task's database creation timestamp. */
final class TaskCreationReportActionBuilder extends ActionBuilder {
  override def build(context: ScenarioContext, next: Action): Action =
    new TaskCreationReportAction(context.coreComponents.statsEngine, next)
}

private final class TaskCreationReportAction(
  override val statsEngine: StatsEngine,
  override val next: Action
) extends ChainableAction {
  override val name: String = "Task_Creation_Latency"

  override def execute(session: Session): Unit = {
    val startedAt = session("publishedAt").as[Long]
    val publishAccepted = session("publishAccepted").asOption[Boolean].contains(true)
    val taskCreated = session("taskCreated").asOption[Boolean].contains(true)
    val (completedAt, status, error) =
      if (!publishAccepted) {
        (
          session("publishCompletedAt").asOption[Long].getOrElse(System.currentTimeMillis()),
          KO,
          Some(session("publishError").asOption[String].getOrElse("Service Bus publication failed"))
        )
      } else if (taskCreated) {
        (session("createdAt").as[Long], OK, None)
      } else {
        (
          session("taskCreationCompletedAt").asOption[Long].getOrElse(System.currentTimeMillis()),
          KO,
          Some("Timed out waiting for task to become visible in the TM database")
        )
      }

    statsEngine.logResponse(session.scenario, session.groups, name, startedAt, completedAt, status, None, error)
    next ! session
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

  /** Obtains valid WA case ids once so concurrent events can be correlated independently. */
  def validCaseIds(): Vector[String] = withConnection { connection =>
    val config = TaskMessageLatencyConfig
    val statement = connection.prepareStatement(config.validCaseIdsSql)
    try {
      statement.setString(1, config.jurisdictionId)
      statement.setString(2, config.caseTypeId)
      statement.setInt(3, config.casePoolSize)
      val result = statement.executeQuery()
      try {
        val ids = Vector.newBuilder[String]
        while (result.next()) Option(result.getString("case_id")).filter(_.nonEmpty).foreach(ids += _)
        ids.result()
      }
      finally result.close()
    } finally statement.close()
  }

  final case class CreatedTask(taskId: String, createdAt: Long)

  /** Returns a task once it is visible in Task Management's database. */
  def createdFor(caseId: String, idempotencyKey: String): Option[CreatedTask] = withConnection { connection =>
    val config = TaskMessageLatencyConfig
    val statement = connection.prepareStatement(config.taskLookupSql)
    try {
      statement.setString(1, caseId)
      statement.setString(2, idempotencyKey)
      val result = statement.executeQuery()
      try Option.when(result.next()) {
        CreatedTask(
          result.getString("task_id"),
          result.getLong("created_epoch_ms")
        )
      }
      finally result.close()
    } finally statement.close()
  }
}

object CaseIdPool {
  private val available = new ConcurrentLinkedQueue[String]()

  def initialize(): Unit = synchronized {
    if (available.isEmpty) {
      val ids = TaskDatabase.validCaseIds()
      require(ids.size == TaskMessageLatencyConfig.casePoolSize,
        s"The test needs ${TaskMessageLatencyConfig.casePoolSize} valid ${TaskMessageLatencyConfig.jurisdictionId}/${TaskMessageLatencyConfig.caseTypeId} case ids for the configured load, but found ${ids.size}")
      available.addAll(ids.asJava)
      println(s"Loaded ${ids.size} valid case ids for exclusive in-flight event correlation")
    }
  }

  def acquire(): Option[String] = Option(available.poll())
  def release(caseId: String): Unit = available.offer(caseId)
}

object TaskLatencyMetrics {
  private val firstPublishedAtNanos = new AtomicLong(0L)
  private val lastPublishedAtNanos = new AtomicLong(0L)
  private val attempted = new LongAdder
  private val published = new LongAdder
  private val publishFailed = new LongAdder
  private val successful = new LongAdder
  private val timedOut = new LongAdder
  private val unavailableCaseIds = new LongAdder
  private val latencies = new ConcurrentLinkedQueue[java.lang.Long]()

  def attempt(): Unit = attempted.increment()
  def publicationAccepted(): Unit = {
    val now = System.nanoTime()
    firstPublishedAtNanos.compareAndSet(0L, now)
    lastPublishedAtNanos.set(now)
    published.increment()
  }
  def publicationRejected(): Unit = publishFailed.increment()
  def caseIdUnavailable(): Unit = unavailableCaseIds.increment()
  def record(latencyMillis: Long): Unit = {
    successful.increment()
    latencies.add(latencyMillis)
  }
  def timeout(): Unit = timedOut.increment()

  private def percentile(sorted: Vector[Long], value: Double): Long =
    if (sorted.isEmpty) 0L else sorted(math.ceil(value * sorted.size).toInt.max(1).min(sorted.size) - 1)

  def summary: String = {
    val values = latencies.iterator().asScala.map(_.longValue()).toVector.sorted
    val average = if (values.isEmpty) 0d else values.sum.toDouble / values.size
    val creationRate = if (published.sum() == 0) 0d else successful.sum() * 100d / published.sum()
    val publishWindowHours = (lastPublishedAtNanos.get() - firstPublishedAtNanos.get()).toDouble / 3600000000000d
    val achievedRate = if (published.sum() <= 1 || publishWindowHours <= 0) 0d else (published.sum() - 1) / publishWindowHours
    f"""Task creation timing summary
       |  configured rate: ${TaskMessageLatencyConfig.eventsPerHour}%.0f events/hour, achieved rate: $achievedRate%.0f events/hour
       |  attempted: ${attempted.sum()}, published: ${published.sum()}, publish failed: ${publishFailed.sum()}, no case id available: ${unavailableCaseIds.sum()}
       |  tasks created: ${successful.sum()}, timed out: ${timedOut.sum()}, creation success: $creationRate%.2f%%
       |  latency ms: min=${values.headOption.getOrElse(0L)}, average=$average%.1f, p50=${percentile(values, 0.50)}, p90=${percentile(values, 0.90)}, p95=${percentile(values, 0.95)}, p99=${percentile(values, 0.99)}, max=${values.lastOption.getOrElse(0L)}""".stripMargin
  }
}

object TaskMessageLatencyJourney {
  private def escapeJson(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")

  // Must match CEH IdempotencyKeyGenerator: uppercase MD5(EventInstanceId + taskId).
  private def idempotencyKey(eventInstanceId: String, taskType: String): String = {
    val bytes = MessageDigest.getInstance("MD5")
      .digest(s"$eventInstanceId$taskType".getBytes(StandardCharsets.UTF_8))
    bytes.iterator.map(byte => f"${byte & 0xff}%02x").mkString.toUpperCase(Locale.ROOT)
  }

  val prepareMessage = exec { session =>
    TaskLatencyMetrics.attempt()
    CaseIdPool.acquire() match {
      case Some(caseId) =>
        val eventInstanceId = UUID.randomUUID().toString
        val messageId = UUID.randomUUID().toString
        val publishedAt = System.currentTimeMillis()
        val config = TaskMessageLatencyConfig
        val expectedIdempotencyKey = idempotencyKey(eventInstanceId, config.expectedTaskType)
        val newStateId = Option(config.newStateId).filter(_.nonEmpty)
          .map(value => s"\"${escapeJson(value)}\"").getOrElse("null")
        val body = s"""{"EventInstanceId":"$eventInstanceId","EventTimeStamp":"${Instant.ofEpochMilli(publishedAt)}","CaseId":"$caseId","JurisdictionId":"${escapeJson(config.jurisdictionId)}","CaseTypeId":"${escapeJson(config.caseTypeId)}","EventId":"${escapeJson(config.eventId)}","PreviousStateId":null,"NewStateId":$newStateId,"UserId":"${escapeJson(config.userId)}","AdditionalData":{"Data":{"roleCategory":"${escapeJson(config.roleCategory)}","workType":"${escapeJson(config.workType)}"},"Definition":{}},"MessageProperties":{"batchId":"gatling-latency"},"HoldUntil":null}"""
        if (config.debug) {
          println(s"Prepared Service Bus message: topic=${config.topic}, caseId=$caseId, messageId=$messageId, eventInstanceId=$eventInstanceId, idempotencyKey=$expectedIdempotencyKey, eventId=${config.eventId}, newStateId=${Option(config.newStateId).filter(_.nonEmpty).getOrElse("null")}, jurisdiction=${config.jurisdictionId}, caseType=${config.caseTypeId}, author=${config.messageAuthor}, messageContext=${config.messageContextPrefix}$caseId, publishedAt=${Instant.ofEpochMilli(publishedAt)}")
        }
        session.setAll("caseId" -> caseId, "messageId" -> messageId, "idempotencyKey" -> expectedIdempotencyKey, "publishedAt" -> publishedAt, "messageBody" -> body, "taskCreated" -> false, "pollCount" -> 0)
      case None =>
        TaskLatencyMetrics.caseIdUnavailable()
        session.markAsFailed.set("caseIdLookupError", "case_pool_exhausted")
    }
  }

  val findCreatedTask = exec { session =>
    val nextPoll = session("pollCount").as[Int] + 1
    Try(TaskDatabase.createdFor(session("caseId").as[String], session("idempotencyKey").as[String])) match {
      case scala.util.Success(Some(task)) => session.setAll(
        "taskCreated" -> true,
        "taskId" -> task.taskId,
        "createdAt" -> task.createdAt,
        "pollCount" -> nextPoll
      )
      case scala.util.Success(None) => session.set("pollCount", nextPoll)
      case scala.util.Failure(error) =>
        println(s"Task lookup failed for caseId=${session("caseId").as[String]}: ${error.getClass.getSimpleName}: ${error.getMessage}")
        session.markAsFailed.set("taskLookupError", error.getClass.getSimpleName)
    }
  }

  val finishLatency = exec { session =>
    val result = if (session("taskCreated").as[Boolean]) {
      val latency = session("createdAt").as[Long] - session("publishedAt").as[Long]
      TaskLatencyMetrics.record(latency)
      if (TaskMessageLatencyConfig.debug) {
        println(s"Created task detected in TM database: caseId=${session("caseId").as[String]}, taskId=${session("taskId").as[String]}, taskType=${TaskMessageLatencyConfig.expectedTaskType}, idempotencyKey=${session("idempotencyKey").as[String]}, createdAt=${Instant.ofEpochMilli(session("createdAt").as[Long])}, latencyMs=$latency")
      }
      session.set("taskCreationLatencyMs", latency)
    } else {
      TaskLatencyMetrics.timeout()
      println(s"Timed out waiting for task: caseId=${session("caseId").as[String]}, messageId=${session("messageId").as[String]}, idempotencyKey=${session("idempotencyKey").as[String]}, eventId=${TaskMessageLatencyConfig.eventId}, expectedTaskType=${TaskMessageLatencyConfig.expectedTaskType}, publishedAt=${Instant.ofEpochMilli(session("publishedAt").as[Long])}, polls=${session("pollCount").as[Int]}")
      session.set("taskCreationCompletedAt", System.currentTimeMillis()).markAsFailed
    }
    session("caseId").asOption[String].foreach(CaseIdPool.release)
    result
  }

}
