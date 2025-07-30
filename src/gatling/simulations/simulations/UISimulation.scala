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
import utilities.AzureKeyVault

import scala.concurrent.duration._
import scala.util.Random

class UISimulation extends Simulation  {

//  val config: Config = ConfigFactory.load()

  val clientSecret = AzureKeyVault.loadClientSecret("ccd-perftest", "ccd-api-gateway-oauth2-client-secret")

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

  val feedTribunalUserData = csv("WA_TribunalUsers.csv").circular
  val feedSeniorTribunalUsers = csv("WA_SeniorTribunalUsers.csv")
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
  val feedETUserData = csv("ETUserData.csv").circular
  val feedETCaseData = csv("ETCaseData.csv")
  val feedSSCSUserData = csv("SSCSUserData.csv").circular
  val feedSSCSCaseData = csv("SSCSCaseData.csv")
  val feedSTUserData = csv("STUserData.csv").circular
  val feedSTCaseData = csv("STCaseData.csv")
  val taskCancelListFeeder = csv("WA_TasksToCancel.csv")

  val randomFeeder = Iterator.continually(Map("cancel-percentage" -> Random.nextInt(100)))

  /* PERFORMANCE TEST CONFIGURATION */
	val iacTargetPerHour: Double = 700 //700
	val cancelTaskTargetPerHour: Double = 500 //300
	val iacCreateTargetPerHour: Double = 1500 //1500
  val civilCompleteTargetPerHour: Double = 200 //200
  val civilJudicialCompleteTargetPerHour: Double = 150 //150
	val judicialTargetPerHour: Double = 360 //360
  val prlTargetPerHour: Double = 130 //130
  val fplTargetPerHour: Double = 335 //335
  val etTargetPerHour: Double = 100 
  val sscsTargetPerHour: Double = 650 //650 
  val sscsCompleteTargetPerHour: Double = 325
  val stTargetPerHour: Double = 50 //50

  //This determines the percentage split of Complete or Cancel Task journeys
//  val completePercentage = 0 //Percentage of Complete Tasks //90

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

  //UI journeys >>

  val CancelTask = scenario("Cancel a Task")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(Homepage.XUIHomePage)
      .feed(feedTribunalUserData)
      .exec(Login.XUILogin)
      .exec(xuiAllWork.allWorkTasks)
      .exec(xuiwa.cancelTask)
      .exec(xuiwa.XUILogout)
    }

  val JudicialUserJourney = scenario("Judicial User Journey")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(Homepage.XUIHomePage)
      .feed(feedJudicialUserData)
      .exec(Login.XUILogin)
      .exec(xuiAllWork.judicialUserAllWork)
      .exec(xuiAllWork.judicialUserOpenCase)
      .exec(xuiAllWork.judicialUserAllocateRole)
      .exec(xuiAllWork.judicialUserRemoveRole)
      .exec(xuiwa.XUILogout)
    }

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
  //New - e2e flows to negate the need for data prep
  ===============================================================================================*/

  def buildScenario(caseType: CcdCaseType, createTask: ChainBuilder, completeTask: ChainBuilder): ScenarioBuilder = {
    scenario(s"${caseType.name} - ${if (createOnly == "off") "Create & Complete Tasks" else "Create Tasks Only"}")
      .exitBlockOnFail {
        exec(_.set("env", env).set("caseType", caseType.caseTypeId))
        .exec(createTask)
//        .exec(_.set("caseId", "1753805466174082"))
        .doIf(createOnly == "off") {
          pause(60.seconds)
          .exec(completeTask)
        }
      }
  }

  val IACScenario = buildScenario(CcdCaseTypes.IA_Asylum, iac.CreateTask.execute, iac.TaskActions.execute)
  val PRLScenario = buildScenario(CcdCaseTypes.PRIVATELAW_PRLAPPS, prl.CreateTask.execute, prl.ActionTask.execute)
  val ETScenario = buildScenario(CcdCaseTypes.EMPLOYMENT_EnglandWales, et.CreateTask.execute, et.ActionTaskET.execute)
  val FPLScenario = buildScenario(CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, fpl.CreateTask.execute, fpl.ActionTaskFPL.execute)
  val CivilScenario = buildScenario(CcdCaseTypes.CIVIL_CIVIL, civil.CreateTask.execute, civil.ActionTaskCivil.execute)

  val STEndToEndCreateAndComplete = scenario("E2E flow ST Citizen Create & Caseworker Complete")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
      .exec(cuiSpecialTribs.cuiHomePage)
      .exec(cuiSpecialTribs.cuiCreateSTCase)
      .pause(60.seconds)
      .feed(feedSTUserData)
      .exec(Homepage.XUIHomePage)
      .exec(Login.XUILogin)
      .exec(xuiSt.SearchCase)
      .exec(xuiSt.ViewCase)
      .exec(xuiwa.AssignTask)
      .exec(xuiSt.EditCase)
      .exec(xuiSt.BuildCase)
      .exec(Logout.XUILogout)
    }

  /*val PRLEndToEndCreateAndComplete = scenario("E2E flow Create PRL Task & Caseworker Complete")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .feed(feedPRLUserData)
        .exec(S2S.s2s("ccd_data"))
//        .exec(IdamLogin.GetIdamToken)
        .exec(ccddatastore.prlCreateCase)
        .exec(ccddatastore.prlApplicationType)
        .exec(ccddatastore.prlWithoutNotice)
        .exec(ccddatastore.prlApplicantDetails)
        .exec(ccddatastore.prlRespondentDetails)
        .exec(ccddatastore.prlFamilyDetails)
        .exec(ccddatastore.prlRelationship)
        .exec(ccddatastore.prlBehaviour)
        .exec(ccddatastore.prlHome)
        .exec(ccddatastore.prlSubmit)
        .pause(60.seconds)
        .exec(Homepage.XUIHomePage)
        .feed(feedPRLTribunalUsers)
        .exec(Login.XUILogin)
        .exec(xuiAllWork.allWorkTasks)
        .exec(xuiAllWork.allWorkTasksHighPriority)
        .exec(xuiPrl.SearchCase)
        .exec(xuiPrl.ViewCase)
        .exec(xuiwa.AssignTask)
        .exec(xuiPrl.AddCaseNumber)
        .exec(xuiPrl.SendToGatekeeper)
        .exec(xuiwa.XUILogout)
    }

  val ETEndToEndCreateAndComplete = scenario("E2E flow Create ET Task & Caseworker Complete")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .feed(feedETUserData)
        .exec(S2S.s2s("ccd_data"))
//        .exec(IdamLogin.GetIdamToken)
        .exec(et.ccdCreateETCase)
        .exec(et.ccdETSubmitDraft)
        .pause(60.seconds)
        .exec(Homepage.XUIHomePage)
        .exec(Login.XUILogin)
        .exec(xuiAllWork.allWorkTasks)
        .exec(xuiAllWork.allWorkTasksHighPriority)
        .exec(xuiEt.SearchCase)
        .exec(xuiEt.ViewCase)
        .doIf("#{taskId.exists()}") {
          exec(xuiwa.AssignTask)
            .exec(xuiEt.etVetting)
            .exec(xuiEt.etPreAcceptance)
        }
        .exec(xuiwa.XUILogout)
    }

  val CivilEndToEndCreateAndComplete = scenario("E2E flow Create Civil Task & Caseworker Complete")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .feed(feedCivilUserData)
        .exec(S2S.s2s("ccd_data"))
//        .exec(IdamLogin.GetIdamToken)
        .exec(S2S.s2s("xui_webapp"))
//        .exec(IdamLogin.GetIdamTokenPayments)
        .exec(ccddatastore.civilCreateCase)
        .pause(60)
        // .feed(feedCivilCaseList) // use this for manually putting a case ID in when running this is debug mode, and you have to run each case event in turn
        .exec(S2S.s2s("civil_service"))
        .exec(ccddatastore.civilAddPayment)
        .pause(60)
        .exec(ccddatastore.civilNotifyClaim)
        .pause(60)
        .exec(ccddatastore.civilNotifyClaimDetails)
        .pause(60)
        .exec(ccddatastore.civilUpdateDate)
        .pause(60)
        .exec(ccddatastore.civilRequestDefaultJudgement)
        .pause(60)
        .exec(Homepage.XUIHomePage)
        .feed(feedCivilJudgeData)
        .exec(Login.XUILogin)
        .exec(xuiAllWork.allWorkTasks)
        .exec(xuiAllWork.allWorkTasksHighPriority)
        // .feed(feedCivilJudicialCases)
        .exec(_.set("jurisdiction", "CIVIL"))
        .exec(xuiSearchChallengedAccess.GlobalSearch)
        .doIf(session => session("accessRequired").as[String].equals("CHALLENGED")) {
          exec(xuiSearchChallengedAccess.JudicialChallengedAccess)
        }
        .exec(xuiJudicialTask.SearchCase)
        .exec(xuiJudicialTask.ViewCase)
        .doIf("#{taskId.exists()}") {
          exec(xuiJudicialTask.AssignTask)
            .exec(xuiJudicialTask.StandardDirectionOrder)
        }
        .exec(xuiwa.XUILogout)
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
              details("XUI_CancelTask").successfulRequests.count.gte((cancelTaskTargetPerHour * 0.9).ceil.toInt),
              details("XUI_Judicial_004_ConfirmRoleAllocation").successfulRequests.count.gte((judicialTargetPerHour * 0.9).ceil.toInt),
              details("XUI_RequestRespondentEvidence_Submit").successfulRequests.count.gte((iacTargetPerHour * 0.9).ceil.toInt),
              details("XUI_AddCaseNumber_Submit").successfulRequests.count.gte((prlTargetPerHour * 0.9).ceil.toInt),
              details("XUI_JudicialSDO_Submit_Request").successfulRequests.count.gte((civilJudicialCompleteTargetPerHour * 0.9).ceil.toInt),
              details("XUI_ReplyToMessage_Submit").successfulRequests.count.gte((fplTargetPerHour * 0.9).ceil.toInt),
              details("XUI_SubmitAcceptance").successfulRequests.count.gte((etTargetPerHour * 0.9).ceil.toInt)
            )
          }
          else{
            Seq(global.successfulRequests.percent.gte(95),
              details("XUI_CancelTask").successfulRequests.count.is(1),
              details("XUI_Judicial_004_ConfirmRoleAllocation").successfulRequests.count.is(1),
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
//        STEndToEndCreateAndComplete.inject(simulationProfile(testType, stTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//        IACScenario.inject(simulationProfile(testType, iacTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//      PRLScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
        ETScenario.inject(simulationProfile(testType, etTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//        FPLScenario.inject(simulationProfile(testType, fplTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//        CivilScenario.inject(simulationProfile(testType, civilCompleteTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//        JudicialUserJourney.inject(simulationProfile(testType, judicialTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//        CancelTask.inject(simulationProfile(testType, cancelTaskTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),

      // CreateSSCSTaskFromCCD.inject(simulationProfile(testType, sscsTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption), //Not onboarded so currently disabled - 19th August 2024

      //Not used for testing
      // getTaskFromCamunda.inject(rampUsers(1) during (1 minute))
      // cancelTaskInTM.inject(rampUsers(1) during (1 minute))
    )
      .maxDuration(75.minutes)
      .protocols(httpProtocol)
  }