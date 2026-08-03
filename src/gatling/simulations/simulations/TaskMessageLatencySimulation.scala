package simulations

import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import scenarios.taskmessages.{ServiceBusSasToken, TaskLatencyMetrics, TaskMessageLatencyConfig, TaskMessageLatencyJourney}

import scala.concurrent.duration._

/**
 * Publishes WA case-event messages through the Service Bus REST endpoint and polls PostgreSQL
 * until the corresponding task exists. This is deliberately a separate, explicitly selected
 * simulation because every virtual user publishes a real message and creates downstream data.
 */
class TaskMessageLatencySimulation extends Simulation {
  TaskMessageLatencyConfig.validate()

  private val config = TaskMessageLatencyConfig
  private val serviceBusBaseUrl = s"https://${ServiceBusSasToken.namespace}.servicebus.windows.net"

  private val protocol = http.baseUrl(serviceBusBaseUrl)
  private val scenarioUnderTest = scenario("Service Bus message to task latency")
    .exitBlockOnFail {
      exec(TaskMessageLatencyJourney.prepareMessage)
        .exec(
          http("ASB_PublishCaseEvent")
            .post(s"/${config.topic}/messages")
            .header("Authorization", "#{sasToken}")
            .header("BrokerProperties", "{\"MessageId\":\"#{messageId}\",\"SessionId\":\"#{caseId}\",\"PartitionKey\":\"#{caseId}\"}")
            .header("jurisdiction_id", config.jurisdictionId)
            .header("case_type_id", config.caseTypeId)
            .header("case_id", "#{caseId}")
            .header("event_id", config.eventId)
            .header("JMSXGroupID", "#{caseId}")
            .header("message_author", config.messageAuthor)
            .header("Content-Type", "application/json")
            .body(StringBody("#{messageBody}"))
            .check(status.is(201))
        )
        .asLongAs(session => !session("taskCreated").as[Boolean] && session("pollCount").as[Int] < config.maxPolls) {
          exec(TaskMessageLatencyJourney.findCreatedTask)
            .pause(config.pollInterval)
        }
        .exec(TaskMessageLatencyJourney.finishLatency)
    }

  private val ratePerSecond = config.ratePerMinute / 60d
  private val injectionProfile: Seq[OpenInjectionStep] =
    Seq(constantUsersPerSec(ratePerSecond).during(config.duration))

  before {
    println(s"Task message latency test: topic=${config.topic}, rate=${config.ratePerMinute}/minute, database=${config.dbHost}/${config.dbName}")
  }

  setUp(
    scenarioUnderTest.inject(injectionProfile)
  ).protocols(protocol)
    .assertions(global.successfulRequests.percent.gte(95))
    .maxDuration(config.duration + (config.maxPolls * config.pollInterval) + 5.minutes)

  after { println(TaskLatencyMetrics.summary) }
}
