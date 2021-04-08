package uk.gov.hmcts.wa.simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef.Proxy
import uk.gov.hmcts.wa.scenarios._
import uk.gov.hmcts.wa.scenarios.utils._
import scala.concurrent.duration._

class UISimulation extends Simulation  {

  val BaseURL = Environment.xuiBaseURL

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
        .exec(xuiwa.openTaskList)
        .exec(xuiwa.OpenTask)
        .exec(xuiwa.EndAppealCaseEvent)
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
      .exec(xuiwa.manageCasesLoginSenior)
      .repeat(8) { //8
        exec(xuiwa.openTaskManager)
        .exec(xuiwa.cancelTask)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
      .exec(xuiwa.XUILogout)
    }

  setUp(
    AssignTask.inject(rampUsers(3) during (5 minutes)), //3
    CompleteTask.inject(rampUsers(6) during (5 minutes)), //6
    CancelTask.inject(rampUsers(4) during (5 minutes)) //4
    
    )
    .protocols(httpProtocol)
}