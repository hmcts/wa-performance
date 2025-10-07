package simulations

import ccd.{CcdCaseType, CcdCaseTypes}
import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import utils._
import scenarios._

import scala.concurrent.duration._

class WASimulation extends Simulation  {

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
  val createOnly = System.getProperty("createonly", "off")
  val env = System.getProperty("env", environment) //manually override the environment aat|perftest e.g. ./gradle gatlingRun -Denv=aat
	/* ******************************** */

  /* PERFORMANCE TEST CONFIGURATION */
	val iacTargetPerHour: Double = 700 //700
  val civilCompleteTargetPerHour: Double = 200 //200
  val prlTargetPerHour: Double = 130 //130
  val fplTargetPerHour: Double = 335 //335
  val etTargetPerHour: Double = 100 
  val sscsTargetPerHour: Double = 650 //650 
  val stTargetPerHour: Double = 50 //50
  val waTargetPerHour: Double = 300

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
    .header("experimental", "true")

	before {
		println(s"Test Type: ${testType}")
		println(s"Test Environment: ${env}")
		println(s"Debug Mode: ${debugMode}")
	}

  /*===============================================================================================
  //New - e2e flows to negate the need for data prep
  ===============================================================================================*/

  def buildScenario(caseType: CcdCaseType, createTask: ChainBuilder, completeTask: ChainBuilder): ScenarioBuilder = {
    scenario(s"${caseType.name} - ${if (createOnly == "off") "Create & Complete Tasks" else "Create Tasks Only"}")
      .exitBlockOnFail {
        exec(_.set("env", env).set("caseType", caseType.caseTypeId))
        .exec(createTask)
        .doIf(createOnly == "off") {
//          pause(60.seconds)
          exec(completeTask)
        }
      }
    }

  val IACScenario = buildScenario(CcdCaseTypes.IA_Asylum, iac.CreateTaskIAC.execute, iac.ActionTaskIAC.execute)
  val PRLScenario = buildScenario(CcdCaseTypes.PRIVATELAW_PRLAPPS, prl.CreateTaskPRL.execute, prl.ActionTaskPRL.execute)
  val ETScenario = buildScenario(CcdCaseTypes.EMPLOYMENT_EnglandWales, et.CreateTaskET.execute, et.ActionTaskET.execute)
  val FPLScenario = buildScenario(CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, fpl.CreateTaskFPL.execute, fpl.ActionTaskFPL.execute)
  val CivilScenario = buildScenario(CcdCaseTypes.CIVIL_CIVIL, civil.CreateTaskCivil.execute, civil.ActionTaskCivil.execute)
  val STScenario = buildScenario(CcdCaseTypes.ST_CIC_CriminalInjuriesCompensation, st.CreateTaskST.execute, st.ActionTaskST.execute)
  val SSCSScenario = buildScenario(CcdCaseTypes.SSCS_Benefit, sscs.CreateTaskSSCS.execute, sscs.ActionTaskSSCS.execute)
  val WAScenario = buildScenario(CcdCaseTypes.WA_WaCaseType, wa.CreateTaskWA.execute, wa.ActionTaskWA.execute)

  //Debugging/Data Gen journeys - NOT USED FOR PERF TESTING!
  /*
    val getTaskFromCamunda = scenario("Camunda Get Task")
      .exec(_.set("env", s"${env}"))
      .exec(S2S.s2s("wa_task_management_api"))
      .repeat(8239) {
        exec(wataskmanagement.CamundaGetCase)
      }

    val cancelTaskInTM = scenario("TM - Cancel Task")
      .exec(_.set("env", s"${env}"))
      .feed(feedSeniorTribunalUsers)
      .exec(S2S.s2s("wa_task_management_api"))
  //    .exec(IdamLogin.GetIdamToken)
      .repeat(16605) {
        feed(taskCancelListFeeder)
        .exec(wataskmanagement.CancelTask)
      }
  */

  /*===============================================================================================
  * Simulation Configuration
  ===============================================================================================*/

  def simulationProfile(simulationType: String, userPerHourRate: Double, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
    val userPerSecRate = userPerHourRate / 3600
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(
            rampUsersPerSec(0.00) to (userPerSecRate) during (rampUpDurationMins.minutes),
            constantUsersPerSec(userPerSecRate) during (testDurationMins.minutes),
            rampUsersPerSec(userPerSecRate) to (0.00) during (rampDownDurationMins.minutes)
          )
        }
        else {
          Seq(atOnceUsers(1))
        }
      case "pipeline" =>
        Seq(rampUsers(numberOfPipelineUsers.toInt) during (2.minutes))
      case _ =>
        Seq(nothingFor(0))
    }
  }

  //defines the test assertions, based on the test type
  def assertions(simulationType: String): Seq[Assertion] = {
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(global.successfulRequests.percent.gte(95),
            details("XUI_RequestRespondentEvidence_Submit").successfulRequests.count.gte((iacTargetPerHour * 0.9).ceil.toInt),
            details("XUI_AddCaseNumber_Submit").successfulRequests.count.gte((prlTargetPerHour * 0.9).ceil.toInt),
            details("XUI_JudicialSDO_Submit_Request").successfulRequests.count.gte((civilCompleteTargetPerHour * 0.9).ceil.toInt),
            details("XUI_ReplyToMessage_Submit").successfulRequests.count.gte((fplTargetPerHour * 0.9).ceil.toInt),
            details("XUI_SubmitAcceptance").successfulRequests.count.gte((etTargetPerHour * 0.9).ceil.toInt)
          )
        }
        else{
          Seq(global.successfulRequests.percent.gte(95),
            details("XUI_RequestRespondentEvidence_Submit").successfulRequests.count.is(1),
            details("XUI_AddCaseNumber_Submit").successfulRequests.count.is(1),
            details("XUI_JudicialSDO_Submit_Request").successfulRequests.count.is(1),
            details("XUI_ReplyToMessage_Submit").successfulRequests.count.is(1),
            details("XUI_SubmitAcceptance").successfulRequests.count.is(1)
          )
        }
      case "pipeline" =>
        Seq(global.successfulRequests.percent.gte(95),
          forAll.successfulRequests.percent.gte(90)
        )
      case _ =>
        Seq()
    }
  }

  setUp(
//    STScenario.inject(simulationProfile(testType, stTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//    IACScenario.inject(simulationProfile(testType, iacTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//    ETScenario.inject(simulationProfile(testType, etTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//    FPLScenario.inject(simulationProfile(testType, fplTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//    CivilScenario.inject(simulationProfile(testType, civilCompleteTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//    PRLScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    WAScenario.inject(simulationProfile(testType, waTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//    SSCSScenario.inject(simulationProfile(testType, sscsTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption), //Not onboarded so currently disabled - 4th August 2025

    //Not used for testing
    // getTaskFromCamunda.inject(rampUsers(1) during (1 minute))
    // cancelTaskInTM.inject(rampUsers(1) during (1 minute))
  )
    .maxDuration(70.minutes)
    .protocols(httpProtocol)
}