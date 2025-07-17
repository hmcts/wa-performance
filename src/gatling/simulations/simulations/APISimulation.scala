package simulations

import _root_.utils._
import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import scenarios._

import scala.concurrent.duration._

class APISimulation extends Simulation {

  val config: Config = ConfigFactory.load()

  /* TEST TYPE DEFINITION */
  /* pipeline = nightly pipeline against the AAT environment (see the Jenkins_nightly file) */
  /* perftest (default) = performance test against the perftest environment */
  val testType = scala.util.Properties.envOrElse("TEST_TYPE", "perftest")

  //set the environment based on the test type
  val environment = testType match {
    case "perftest" => "perftest"
    case "pipeline" => "perftest"
    case _ => "**INVALID**"
  }

  /* ******************************** */
  /* ADDITIONAL COMMAND LINE ARGUMENT OPTIONS */
  val debugMode = System.getProperty("debug", "off") //runs a single user e.g. ./gradle gatlingRun -Ddebug=on (default: off)
  val env = System.getProperty("env", environment) //manually override the environment aat|perftest e.g. ./gradle gatlingRun -Denv=aat
  /* ******************************** */

  val feedSearchUsers = csv("WA_SearchUsers.csv").circular

  /* PERFORMANCE TEST CONFIGURATION */
  val searchTargetPerHour: Double = 100 //700

  val rampUpDurationMins = 5
  val rampDownDurationMins = 5
  val testDurationMins = 60 //60

  val numberOfPipelineUsers = 5
  val pipelinePausesMillis: Long = 3000 //3 seconds

  //Determine the pause pattern to use:
  //Performance test = use the pauses defined in the scripts
  //Pipeline = override pauses in the script with a fixed value (pipelinePauseMillis)
  //Debug mode = disable all pauses
  val pauseOption: PauseType = debugMode match {
    case "off" if testType == "perftest" => constantPauses
    case "off" if testType == "pipeline" => customPauses(pipelinePausesMillis)
    case _ => constantPauses //disabledPauses
  }

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(Environment.xuiBaseURL.replace("#{env}", s"${env}"))
    .doNotTrackHeader("1")

  before {
    println(s"Test Type: ${testType}")
    println(s"Test Environment: ${env}")
    println(s"Debug Mode: ${debugMode}")
  }

  val WASearchTasks = scenario("Work Allocation API - Search for tasks")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .feed(feedSearchUsers)
      .exec(S2S.s2s("wa_task_management_api"))
//      .exec(IdamLogin.GetIdamToken)
      .repeat(1) {
        exec(wataskmanagement.SearchTask)
      }
    }

  /*===========================================================================================
  * Simulation Configuration
  ===========================================================================================*/

  def simulationProfile(simulationType: String, userPerHourRate: Double, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
    val userPerSecRate = userPerHourRate / 3600
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(
            rampUsersPerSec(0.00) to (userPerSecRate) during (rampUpDurationMins minutes),
            constantUsersPerSec(userPerSecRate) during (testDurationMins minutes),
            rampUsersPerSec(userPerSecRate) to (0.00) during (rampDownDurationMins minutes)
          )
        }
        else {
          Seq(atOnceUsers(1))
        }
      case "pipeline" =>
        Seq(rampUsers(numberOfPipelineUsers.toInt) during (2 minutes))
      case _ =>
        Seq(nothingFor(0))
    }
  }


  setUp(
    WASearchTasks.inject(simulationProfile(testType, searchTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)
  )
  .maxDuration(75 minutes)
  .protocols(httpProtocol)


}
