package uk.gov.hmcts.wa.simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios._
import uk.gov.hmcts.wa.scenarios.utils._
import scala.concurrent.duration._

class APISimulation extends Simulation  {

  val BaseURL = Environment.baseURL

  val feedCompleteTaskListFeeder = csv("WA_TasksToComplete.csv")
  val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular
  val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
  val taskListFeeder = csv("WA_TaskList.csv").circular
  val feedIACUserData = csv("IACUserData.csv").circular

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")

  val WAGetTask = scenario("Work Allocation API - Get Task")
    .repeat(1) {
      feed(feedWASeniorUserData)
      .exec(S2S.s2s("wa_task_management_api"))
      .exec(IdamLogin.GetIdamToken)
      .repeat(1) { 
        feed(taskListFeeder)
        .exec(wataskmanagement.GetTask)
      }
    }

  val IACCaseCreate = scenario("IAC Case Create via CCD")
    .repeat(1) {
      feed(feedIACUserData)
      .exec(S2S.s2s("ccd_data"))
      .exec(IdamLogin.GetIdamToken)
      .repeat(1) {  //10
        exec(ccddatastore.ccdCreateCase)
        .exec(ccddatastore.ccdSubmitAppeal)
        // .exec(WaitforNextIteration.waitforNextIteration)
      }
    }

  val WACompleteTask = scenario("Work Allocation API - Complete a Task")
    .repeat(1) {
      feed(feedWASeniorUserData)
      .exec(S2S.s2s("wa_task_management_api"))
      .exec(IdamLogin.GetIdamToken)
      .repeat(1) { //6
        feed(feedCompleteTaskListFeeder)
        .exec(wataskmanagement.GetTaskForCompletion)
        .exec(wataskmanagement.PostAssignTask)
        .exec(wataskmanagement.CompleteTask)
        // .exec(WaitforNextIteration.waitforNextIteration)
      }
    }

  val WACancelTask = scenario("Work Allocation API - Cancel a Task")
    .repeat(1) {
      feed(feedWASeniorUserData)
      .exec(S2S.s2s("wa_task_management_api"))
      .exec(IdamLogin.GetIdamToken)
      .repeat(500) { //8
        // exec(wataskmanagement.GetTask)
        exec(wataskmanagement.CancelTask)
        // .exec(WaitforNextIteration.waitforNextIteration)
      }
    }

  val WAAssignTask = scenario ("Assign a Task")
    .repeat(1) {
      feed(feedWASeniorUserData)
      .exec(S2S.s2s("wa_task_management_api"))
      .exec(IdamLogin.GetIdamToken)
      .repeat(1) { 
        feed(taskListFeeder)
        .exec(wataskmanagement.PostAssignTask)
      }
    }
  val CamundaGetCase = scenario("Camunda DB - Get Case details")
    .repeat(1) {
      exec(S2S.s2s("wa_task_management_api"))
      .repeat(1) { //5000
        exec(wataskmanagement.CamundaGetCase)
      }
    }

  //Pipeline Scenario
  val WAPipeline = scenario("Create Case, Create Task")
      .exec(ccddatastore.ccdIdamLogin)
      .exec(ccddatastore.ccdCreateCase)
      .exec(ccddatastore.ccdSubmitAppeal)
      .pause(10)
      .exec(Environment.ClearSessionVariables)
      .exec(wataskmanagement.WAS2SLogin)
      .exec(wataskmanagement.WATaskS2SLogin)
      .exec(wataskmanagement.WASeniorIdamLogin)
      .exec(wataskmanagement.CreateTask)
      .pause(20)
      .exec(wataskmanagement.CamundaGetCase)
      .exec(wataskmanagement.GetTask)
      .exec(wataskmanagement.PostAssignTask)
      .exec(wataskmanagement.CompleteTask)

  val TaskManagerTests = scenario("Creates cases & tasks for Task Manager searches/rendering")
    // .repeat(1) {
    //   exec(ccddatastore.ccdIdamLogin)
    //   .exec(ccddatastore.ccdCreateCase)
    //   .exec(ccddatastore.ccdSubmitAppeal)
    //   .pause(10)
    //   }
    .repeat(1) {//Update for required number of tasks
      exec(S2S.s2s("wa_case_event_handler"))
      .repeat(1) {
        exec(wataskmanagement.CreateTask)
      }
    }

  val GetAllTasks = scenario("WA - Get All Tasks")
    .repeat(1) {
      feed(feedWASeniorUserData)
      .exec(S2S.s2s("wa_case_event_handler"))
      .exec(IdamLogin.GetIdamToken)
      .repeat(1) { 
        exec(wataskmanagement.GetAllTasks)
      }
    }

  setUp(
    // TaskManagerTests.inject(rampUsers(1) during (1 minutes))
    // WACompleteTask.inject(rampUsers(1) during (1 minutes))
    WAAssignTask.inject(rampUsers(1) during (1 minutes))
    // GetAllTasks.inject(rampUsers(1) during (1 minutes))
    // CamundaGetCase.inject(rampUsers(1) during (1 minutes))
    // WAGetTask.inject(rampUsers(1) during (1 minutes))

    //Scenarios required for perf test
    // IACCaseCreate.inject(rampUsers(1) during (5 minutes)),
    // WACompleteTask.inject(rampUsers(6) during (5 minutes)),
    // WACancelTask.inject(rampUsers(4) during (5 minutes)),
    // WAGetTask.inject(rampUsers(80) during (7 minutes))
  )
    .protocols(httpProtocol)
}