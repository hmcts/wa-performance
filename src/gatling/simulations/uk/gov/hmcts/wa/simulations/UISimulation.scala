package uk.gov.hmcts.wa.simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef.Proxy
import uk.gov.hmcts.wa.scenarios._
import uk.gov.hmcts.wa.scenarios.utils._
import scala.concurrent.duration._

class UISimulation extends Simulation  {

  val BaseURL = Environment.xuiBaseURL
  val feedTribunalUserData = csv("WA_TribunalUsers.csv")
  val feedJudicialUserData = csv("WA_JudicialUsers.csv")
  val feedIACUserData = csv("IACUserData.csv").circular
  val feedCaseList = csv("WA_R2Cases.csv")

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")

  val CompleteTask = scenario("Complete a Task")
    .repeat(1) {
      exec(xuiwa.manageCasesHomePage)
      .exec(xuiwa.manageCasesLoginSenior)
      .repeat(6) { //6
        exec(xuiwa.openTaskManager)
        .exec(xuiwa.assignTaskForCompletion)
        .exec(xuiwa.completeTask)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
      .exec(xuiwa.XUILogout)
    }

  val AssignTask = scenario("Assign a Task")
    .repeat(1) {
      exec(xuiwa.manageCasesHomePage)
      .exec(xuiwa.manageCasesLoginSenior)
      .repeat(12) { //12
        exec(xuiwa.openTaskManager)
        .exec(xuiwa.assignTask)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
      .exec(xuiwa.XUILogout)
    }

  val CancelTask = scenario("Cancel a Task")
    .repeat(1) {
      exec(xuiwa.manageCasesHomePage)
      .feed(feedTribunalUserData)
      .exec(xuiwa.manageCasesLogin)
      .repeat(1904) { //8
        // exec(xuiwa.openTaskManager)
        exec(xuiwa.cancelTask)
        // .exec(WaitforNextIteration.waitforNextIteration)
      }
      .exec(xuiwa.XUILogout)
    }

  val R2AssignAndCompleteTasks = scenario("Assign a Task and Complete it")
    .repeat(1) {
      exec(xuiwa.manageCasesHomePage)
      .feed(feedTribunalUserData)
      .exec(xuiwa.manageCasesLogin)
      .repeat(10) { //10
        exec(xuiAllWork.allWorkTasks)
        .feed(feedCaseList)
        .exec(xuiAllWork.allWorkViewTask)
        // .exec(xuiwa.AssignRoles)
        .exec(xuiwa.RequestRespondentEvidence)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
      .exec(xuiwa.XUILogout)
    }

  val CreateTaskFromCCD = scenario("Creates cases & tasks for Task Manager searches/rendering")
    .feed(feedIACUserData)
    .exec(S2S.s2s("ccd_data"))
    .exec(IdamLogin.GetIdamToken)
    .repeat(100) { //100
      exec(ccddatastore.ccdCreateCase)
      .exec(ccddatastore.ccdSubmitAppeal)
      .exec(ccddatastore.ccdRequestHomeOfficeData)
    }


  val R2CancelTask = scenario("Cancel a Task")
    .repeat(1) {
      exec(xuiwa.manageCasesHomePage)
      .feed(feedTribunalUserData)
      .exec(xuiwa.manageCasesLogin)
      .repeat(8) { //8
        exec(xuiMyWork.AvailableTasks)
        .exec(xuiwa.cancelTask)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
      .exec(xuiwa.XUILogout)
    }

  val R2JudicialUserJourney = scenario("Judicial User Journey")
    .repeat(1) {
      exec(xuiwa.manageCasesHomePage)
      .feed(feedJudicialUserData)
      .exec(xuiwa.manageCasesLogin)
      .repeat(10) { //10
        exec(xuiAllWork.judicialUserAllWork)
        .exec(xuiAllWork.judicialUserOpenCase)
        .exec(xuiAllWork.judicialUserAllocateRole)
        .exec(xuiAllWork.judicialUserRemoveRole)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
      .exec(xuiwa.XUILogout)
    }

  
  setUp(
    R2AssignAndCompleteTasks.inject(rampUsers(60) during (10 minutes)), //60 during 10
    R2CancelTask.inject(rampUsers(5) during (20 minutes)),
    CreateTaskFromCCD.inject(rampUsers(15) during (10 minutes)),
    R2JudicialUserJourney.inject(rampUsers(36) during (2 minutes))
    )
    .maxDuration(60 minutes)
    .protocols(httpProtocol)

  // setUp(
  //   R2CancelTask.inject(rampUsers(1) during (1 minutes)).disablePauses
  //   )
  //   .protocols(httpProtocol)

  // setUp(
    // R2AssignAndCompleteTasks.inject(rampUsers(5) during (1 minutes)),
    // R2CancelTask.inject(rampUsers(5) during (1 minutes)),
    // CreateTaskFromCCD.inject(rampUsers(5) during (1 minutes)),
    // R2JudicialUserJourney.inject(rampUsers(3) during (1 minutes))
    // )
    // .maxDuration(5 minutes)
    // .protocols(httpProtocol)
    // .disablePauses
    
}