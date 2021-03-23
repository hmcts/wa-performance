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
    .proxy(Proxy("proxyout.reform.hmcts.net", 8080).httpsPort(8080))
    .doNotTrackHeader("1")

  val WAGetTask = scenario("Work Allocation API - Get Task")
    .repeat(1) {
      exec(ccddatastore.ccdIdamLogin)
      .repeat(1) { 
        exec(wataskmanagement.GetTask)
      }
    }

  val IACCaseCreate = scenario("IAC Case Create via CCD")
    .repeat(1) {
      exec(ccddatastore.ccdIdamLogin)
      .repeat(1) { 
        exec(ccddatastore.ccdCreateCase)
        .exec(ccddatastore.ccdSubmitAppeal)
      }
    }

  val WAPostRetrieveTask = scenario("Work Allocation API - POST Retrieve Task")
    .repeat(1) {
      exec(ccddatastore.ccdIdamLogin)
      .repeat(1) { 
        exec(wataskmanagement.PostTaskRetrieve)
      }
    }

  setUp(
    IACCaseCreate.inject(rampUsers(1) during (1 minutes))
    // WAPostRetrieveTask.inject(rampUsers(1) during (1 minutes))
    // WAGetTask.inject(rampUsers(1) during (1 minutes))
  )
    .protocols(httpProtocol)
}