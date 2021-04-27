package uk.gov.hmcts.wa.simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef.Proxy
import uk.gov.hmcts.wa.scenarios._
import uk.gov.hmcts.wa.scenarios.utils._
import scala.concurrent.duration._

class APISimulation extends Simulation  {

  val BaseURL = Environment.baseURL

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")

  val WAGetTask = scenario("Work Allocation API - Get Task")
    .repeat(1) {
      exec(wataskmanagement.WAS2SLogin)
      .exec(wataskmanagement.WASeniorIdamLogin)
      .repeat(6) { 
        exec(wataskmanagement.GetTaskForSearches)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
    }

  val WASearchCompletable = scenario("Work Allocation API - Search for completable tasks")
    .repeat(1) {
      exec(wataskmanagement.WAS2SLogin)
      .exec(wataskmanagement.WASeniorIdamLogin)
      .repeat(1) { 
        exec(wataskmanagement.PostTaskSearchCompletable)
      }
    }

  val IACCaseCreate = scenario("IAC Case Create via CCD")
    .repeat(1) {
      exec(ccddatastore.ccdIdamLogin)
      .repeat(50) {  //10
        exec(ccddatastore.ccdCreateCase)
        .exec(ccddatastore.ccdSubmitAppeal)
        // .exec(WaitforNextIteration.waitforNextIteration)
      }
    }

  val WAPostRetrieveTask = scenario("Work Allocation API - POST Retrieve Task")
    .repeat(1) {
      exec(ccddatastore.ccdIdamLogin)
      .repeat(1) { 
        exec(wataskmanagement.PostTaskRetrieve)
      }
    }

  val WAClaimTask = scenario("Work Allocation API - Claim Task")
    .repeat(1) {
      exec(wataskmanagement.WAS2SLogin)
      .exec(wataskmanagement.WASeniorIdamLogin)
      .repeat(1) { 
        exec(wataskmanagement.ClaimTask)
      }
    }

  val WAUnclaimTask = scenario("Work Allocation API - Unclaim Task")
    .repeat(1) {
      exec(wataskmanagement.WAS2SLogin)
      .exec(wataskmanagement.WASeniorIdamLogin)
      .repeat(1) { 
        exec(wataskmanagement.UnclaimTask)
      }
    }

  val WAClaimUnclaimTask = scenario("Work Allocation API - Claim and then Unclaim a Task")
    .repeat(1) {
      exec(wataskmanagement.WAS2SLogin)
      .exec(wataskmanagement.WASeniorIdamLogin)
      .repeat(1) { 
        exec(wataskmanagement.ClaimTask)
        .exec(wataskmanagement.UnclaimTask)
      }
    }

  val WACompleteTask = scenario("Work Allocation API - Complete a Task")
    .repeat(1) {
      exec(wataskmanagement.WAS2SLogin)
      .exec(wataskmanagement.WASeniorIdamLogin)
      .repeat(6) {
        exec(wataskmanagement.GetTaskForCompletion)
        .exec(wataskmanagement.PostAssignTask)
        .exec(wataskmanagement.CompleteTask)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
    }

  val WACancelTask = scenario("Work Allocation API - Cancel a Task")
    .repeat(1) {
      exec(wataskmanagement.WAS2SLogin)
      .exec(wataskmanagement.WASeniorIdamLogin)
      .repeat(8) { //8
        // exec(wataskmanagement.GetTask)
        exec(wataskmanagement.CancelTask)
        .exec(WaitforNextIteration.waitforNextIteration)
      }
    }

  val CamundaGetCase = scenario("Camunda DB - Get Case details")
    .repeat(1) {
      exec(wataskmanagement.WAS2SLogin)
      .repeat(123) {
        exec(wataskmanagement.CamundaGetCase)
      }
    }

  setUp(
    // IACCaseCreate.inject(rampUsers(10) during (1 minutes))
    // WAGetTask.inject(rampUsers(1) during (1 minutes))
    // WAPostRetrieveTask.inject(rampUsers(1) during (1 minutes))
    // WASearchCompletable.inject(rampUsers(1) during (1 minutes))
    // WAUnclaimTask.inject(rampUsers(1) during (1 minutes))
    // WAClaimUnclaimTask.inject(rampUsers(1) during (1 minutes))
    // WACancelTask.inject(rampUsers(1) during (1 minutes))
    CamundaGetCase.inject(rampUsers(1) during (1 minutes))

    //Scenarios required for perf test
    // IACCaseCreate.inject(rampUsers(4) during (5 minutes)),
    // WACompleteTask.inject(rampUsers(6) during (5 minutes)),
    // WACancelTask.inject(rampUsers(4) during (5 minutes)),
    // WAGetTask.inject(rampUsers(80) during (7 minutes))
  )
    .protocols(httpProtocol)
}