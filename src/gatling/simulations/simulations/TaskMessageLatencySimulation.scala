package simulations

import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.scenario.Simulation
import scenarios.taskmessages.{CaseIdPool, ServiceBusPublishActionBuilder, ServiceBusPublisher, TaskCreationReportActionBuilder, TaskLatencyMetrics, TaskMessageLatencyConfig, TaskMessageLatencyJourney}

import scala.concurrent.duration._

/**
 * Publishes WA case-event messages through the Service Bus REST endpoint and polls PostgreSQL
 * until the corresponding task exists. This is deliberately a separate, explicitly selected
 * simulation because every virtual user publishes a real message and creates downstream data.
 */
class TaskMessageLatencySimulation extends Simulation {
  TaskMessageLatencyConfig.validate()

  private val config = TaskMessageLatencyConfig
  private val scenarioUnderTest = scenario("Service Bus message to task latency")
    .exec(TaskMessageLatencyJourney.prepareMessage)
    .exitHereIfFailed
    .exec(new ServiceBusPublishActionBuilder)
    .doIf("#{publishAccepted}") {
      asLongAs(session => !session("taskCreated").as[Boolean] && session("pollCount").as[Int] < config.maxPolls) {
        exec(TaskMessageLatencyJourney.findCreatedTask)
          .pause(config.pollInterval)
      }
      .exec(TaskMessageLatencyJourney.finishLatency)
    }
    .exec(new TaskCreationReportActionBuilder)

  private val ratePerSecond = config.eventsPerHour / 3600d
  private val injectionProfile: Seq[OpenInjectionStep] =
    if (config.debug) Seq(atOnceUsers(config.debugEventCount))
    else Seq(constantUsersPerSec(ratePerSecond).during(config.duration))

  before {
    CaseIdPool.initialize()
    ServiceBusPublisher.initialize()
    println(s"Task message latency test: publisher=Azure Service Bus SDK, topic=${config.topic}, author=${config.messageAuthor}, messageContextPrefix=${config.messageContextPrefix}, jurisdiction=${config.jurisdictionId}, caseType=${config.caseTypeId}, event=${config.eventId}, expectedTaskType=${config.expectedTaskType}, rate=${config.eventsPerHour}/hour, duration=${if (config.debug) s"${config.debugEventCount} event(s) at once (debug)" else config.duration}, database=${config.dbUrl}")
  }

  setUp(
    scenarioUnderTest.inject(injectionProfile)
  ).maxDuration((if (config.debug) Duration.Zero else config.duration) + (config.maxPolls * config.pollInterval) + 5.minutes)

  after {
    println(TaskLatencyMetrics.summary)
    ServiceBusPublisher.close()
  }
}
