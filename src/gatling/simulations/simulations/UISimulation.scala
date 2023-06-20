package simulations

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.pause.PauseType
import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.controller.inject.open.OpenInjectionStep
import scenarios._
import utils._
import scala.concurrent.duration._

class UISimulation extends Simulation  {

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

  val feedTribunalUserData = csv("WA_TribunalUsers.csv").circular
  val feedJudicialUserData = csv("WA_JudicialUsers.csv").circular
  val feedIACUserData = csv("IACUserData.csv").circular
  val feedCivilUserData = csv("CivilUserData.csv").circular
  val feedCivilJudgeData = csv("CivilJudicialUserData.csv").circular
  val feedPRLUserData = csv("PRLUserData.csv").circular
  val feedIACCaseList = csv("IACCaseData.csv")
  val feedCivilCaseList = csv("CivilCaseData.csv")
  val feedCivilJudicialCases = csv("CivilJudicialCaseData.csv")
  val feedPRLCaseData = csv("PRLCaseData.csv")
  val feedPRLTribunalUsers = csv("PRLTribunalUserData.csv").circular
  val feedFPLUserData = csv("FPLUserData.csv").circular
  val feedWAFPLUserData = csv("WA_FPLCTSCUsers.csv").circular
  val feedFPLCaseData = csv("FPLCaseData.csv")

	/* PERFORMANCE TEST CONFIGURATION */
	val assignAndCompleteTargetPerHour: Double = 700 //700
	val cancelTaskTargetPerHour: Double = 300 //300
	val iacCreateTargetPerHour: Double = 1500 //1500
  val civilCompleteTargetPerHour: Double = 200 //200
  val civilJudicialCompleteTargetPerHour: Double = 150 //150
	val judicialTargetPerHour: Double = 360 //360
  val prlTargetPerHour: Double = 130 //130
  val fplTargetPerHour: Double = 335 //335

	val rampUpDurationMins = 5
	val rampDownDurationMins = 5
	val testDurationMins = 60

	val numberOfPipelineUsers = 5
	val pipelinePausesMillis: Long = 3000 //3 seconds

	//Determine the pause pattern to use:
	//Performance test = use the pauses defined in the scripts
	//Pipeline = override pauses in the script with a fixed value (pipelinePauseMillis)
	//Debug mode = disable all pauses
	val pauseOption: PauseType = debugMode match {
		case "off" if testType == "perftest" => constantPauses
		case "off" if testType == "pipeline" => customPauses(pipelinePausesMillis)
		case _ => disabledPauses
	}

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(Environment.xuiBaseURL.replace("#{env}", s"${env}"))
    .doNotTrackHeader("1")

	before {
		println(s"Test Type: ${testType}")
		println(s"Test Environment: ${env}")
		println(s"Debug Mode: ${debugMode}")
	}

  val IACAssignAndCompleteTasks = scenario("Assign an IAC Task and Complete it")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(xuiwa.manageCasesHomePage)
      .feed(feedTribunalUserData)
      .exec(xuiwa.manageCasesLogin)
      // .exec(xuiAllWork.allWorkTasks)
      .feed(feedIACCaseList)
      .exec(_.set("jurisdiction", "IA"))
      .exec(xuiSearchChallengedAccess.GlobalSearch)
      .doIf(session => session("accessRequired").as[String].equals("CHALLENGED")) {
        exec(xuiSearchChallengedAccess.ChallengedAccess)
      }
      .exec(xuiSearchChallengedAccess.ViewCase)
      .exec(xuiwa.ViewTasksTab)
      // .exec(xuiwa.AssignRoles)
      .exec(xuiwa.AssignTask)
      .exec(xuiwa.RequestRespondentEvidence)
      .exec(xuiwa.XUILogout)
    }

  val CivilAssignAndCompleteTask = scenario("Assign a Civil Task and Complete it")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(xuiwa.manageCasesHomePage)
      .feed(feedCivilJudgeData)
      .exec(xuiwa.manageCasesLogin)
      .exec(xuiAllWork.allWorkTasks)
      .exec(xuiAllWork.allWorkTasksHighPriority)
      .feed(feedCivilJudicialCases)
      .exec(_.set("jurisdiction", "CIVIL"))
      .exec(xuiSearchChallengedAccess.GlobalSearch)
      .doIf(session => session("accessRequired").as[String].equals("CHALLENGED")) {
        exec(xuiSearchChallengedAccess.JudicialChallengedAccess)
      }
      .exec(xuiSearchChallengedAccess.ViewCase)
      .doIf("#{taskId.exists()}") {
        exec(xuiJudicialTask.AssignTask)
        .exec(xuiJudicialTask.StandardDirectionOrder)
      }
      .exec(xuiwa.XUILogout)
    }

  val PRLAssignAndCompleteTasks = scenario("Assign a PRL Task and Complete it")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(xuiwa.manageCasesHomePage)
      .feed(feedPRLTribunalUsers)
      .exec(xuiwa.manageCasesLogin)
      .feed(feedPRLCaseData)
      .exec(xuiPrl.SearchCase)
      .exec(xuiPrl.ViewCase)
      .exec(xuiwa.AssignTask)
      .exec(xuiPrl.AddCaseNumber)
      // .pause(30) //TO DO - add a do while to check if the next task is available, otherwise wait and then re-check
      // .exec(xuiwa.ViewTasksTab)
      // .exec(xuiwa.AssignTask)
      // .exec(xuiPrl.SendToGatekeeper)
      .exec(xuiwa.XUILogout)
    }

  //To do - complete this flow in the UI with PerfSupervisorCW_001@justice.gov.uk
  val FPLAssignAndCompleteTasks = scenario("Assign an FPL Task and Complete it")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(xuiwa.manageCasesHomePage)
      .feed(feedWAFPLUserData)
      .exec(xuiwa.manageCasesLogin)
      .feed(feedFPLCaseData)
    }

  val CreateIACTaskFromCCD = scenario("Creates IAC cases & tasks in Task Manager")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .feed(feedIACUserData)
      .exec(S2S.s2s("ccd_data"))
      .exec(IdamLogin.GetIdamToken)
      .exec(ccddatastore.ccdCreateIACCase)
      .exec(ccddatastore.ccdIACSubmitAppeal)
      .exec(ccddatastore.ccdIACRequestHomeOfficeData)
    }

  //It's not possible to run this E2E in debug mode - pauses are required between each stage in order to allow 
  //the Civil service account to process the case data before it's ready for the next event - this usually takes up to a minute
  val CreateCivilDJTaskFromCCD = scenario("Creates Civil case, case events & a Default Judgement task for Judicial User")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .feed(feedCivilUserData)
      .exec(S2S.s2s("ccd_data"))
      .exec(IdamLogin.GetIdamToken)
      .repeat(1) {
        exec(ccddatastore.civilCreateCase)
        .pause(60)
        // feed(feedCivilCaseList) // use this for manually putting a case ID in when running this is debug mode, and you 
        // have to run each case event in turn
        .exec(ccddatastore.civilNotifyClaim)
        .pause(60)
        .exec(ccddatastore.civilNotifyClaimDetails)
        .pause(60)
        .exec(ccddatastore.civilUpdateDate)
        .exec(ccddatastore.civilRequestDefaultJudgement)
      }
    }

  val CreatePRLTaskFromCCD = scenario("Creates a PRL FL401 case & task in Task Manager")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .feed(feedPRLUserData)
      .exec(S2S.s2s("ccd_data"))
      .exec(IdamLogin.GetIdamToken)
      .repeat(1) {
        exec(ccddatastore.prlCreateCase)
        .exec(ccddatastore.prlApplicationType)
        .exec(ccddatastore.prlWithoutNotice)
        .exec(ccddatastore.prlApplicantDetails)
        .exec(ccddatastore.prlRespondentDetails)
        .exec(ccddatastore.prlFamilyDetails)
        .exec(ccddatastore.prlRelationship)
        .exec(ccddatastore.prlBehaviour)
        .exec(ccddatastore.prlHome)
        .exec(ccddatastore.prlSubmit)
      }
    }

  val CreateFPLTaskFromCCD = scenario("Creates an FPL case & task in Task Manager")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .feed(feedFPLUserData)
      .exec(S2S.s2s("ccd_data"))
      .exec(IdamLogin.GetIdamToken)
      .exec(fpl.ccdCreateFPLCase)
      .exec(fpl.ccdFPLOrdersNeeded)
      .exec(fpl.ccdFPLHearingNeeded)
      .exec(fpl.ccdFPLEnterGrounds)
      .exec(fpl.ccdFPLEnterLocalAuthority)
      .exec(fpl.ccdFPLEnterChildren)
      .exec(fpl.ccdFPLEnterRespondents)
      .exec(fpl.ccdFPLOtherProposal)
      .exec(fpl.ccdFPLSubmitApplication)
      .feed(feedWAFPLUserData)
      .exec(S2S.s2s("ccd_data"))
      .exec(IdamLogin.GetIdamToken)
      .exec(fpl.ccdSendMessage)
    }

  val CancelTask = scenario("Cancel a Task")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(xuiwa.manageCasesHomePage)
      .feed(feedTribunalUserData)
      .exec(xuiwa.manageCasesLogin)
      // .exec(xuiMyWork.AvailableTasks)
      .exec(xuiAllWork.allWorkTasks)
      .exec(xuiwa.cancelTask)
      .exec(xuiwa.XUILogout)
    }

  val JudicialUserJourney = scenario("Judicial User Journey")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(xuiwa.manageCasesHomePage)
      .feed(feedJudicialUserData)
      .exec(xuiwa.manageCasesLogin)
      .exec(xuiAllWork.judicialUserAllWork)
      .exec(xuiAllWork.judicialUserOpenCase)
      .exec(xuiAllWork.judicialUserAllocateRole)
      .exec(xuiAllWork.judicialUserRemoveRole)
      .exec(xuiwa.XUILogout)
    }

  val getTaskFromCamunda = scenario("Camunda Get Task")
    .exec(_.set("env", s"${env}"))
    .exec(S2S.s2s("wa_task_management_api"))
    .repeat(1555) {
      exec(wataskmanagement.CamundaGetCase)
    }

	/*===============================================================================================
	* Simulation Configuration
	===============================================================================================*/

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

  //defines the test assertions, based on the test type
  def assertions(simulationType: String): Seq[Assertion] = {
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(global.successfulRequests.percent.gte(95),
            details("XUI_CancelTask").successfulRequests.count.gte((cancelTaskTargetPerHour * 0.9).ceil.toInt),
            details("XUI_Judicial_004_ConfirmRoleAllocation").successfulRequests.count.gte((judicialTargetPerHour * 0.9).ceil.toInt),
            details("XUI_RequestRespondentEvidence_Submit").successfulRequests.count.gte((assignAndCompleteTargetPerHour * 0.9).ceil.toInt),
            details("XUI_AddCaseNumber_Submit").successfulRequests.count.gte((assignAndCompleteTargetPerHour * 0.9).ceil.toInt),
            details("XUI_JudicialSDO_Submit_Request").successfulRequests.count.gte((assignAndCompleteTargetPerHour * 0.9).ceil.toInt)
          )
        }
        else{
          Seq(global.successfulRequests.percent.gte(95),
            details("XUI_CancelTask").successfulRequests.count.is(1),
            details("XUI_Judicial_004_ConfirmRoleAllocation").successfulRequests.count.is(1),
            details("XUI_RequestRespondentEvidence_Submit").successfulRequests.count.is(1),
            details("XUI_AddCaseNumber_Submit").successfulRequests.count.is(1),
            details("XUI_JudicialSDO_Submit_Request").successfulRequests.count.is(1)
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
    IACAssignAndCompleteTasks.inject(simulationProfile(testType, assignAndCompleteTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    PRLAssignAndCompleteTasks.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    CivilAssignAndCompleteTask.inject(simulationProfile(testType, civilJudicialCompleteTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    FPLAssignAndCompleteTasks.inject(simulationProfile(testType, fplTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    CancelTask.inject(simulationProfile(testType, cancelTaskTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    JudicialUserJourney.inject(simulationProfile(testType, judicialTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    CreateCivilDJTaskFromCCD.inject(simulationProfile(testType, civilCompleteTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    CreateIACTaskFromCCD.inject(simulationProfile(testType, iacCreateTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    CreatePRLTaskFromCCD.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    CreateFPLTaskFromCCD.inject(simulationProfile(testType, fplTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)

    // getTaskFromCamunda.inject(rampUsers(1) during (1 minute))
    )
    // .maxDuration(60 minutes)
    .protocols(httpProtocol)
}