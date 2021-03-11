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

  val IACCaseCreate = scenario("IAC Case Create via CCD")
    .repeat(1) {
      exec(ccddatastore.ccdIdamLogin)
      .repeat(1) { 
        exec(ccddatastore.ccdCreateCase)
      }
    }

  setUp(
    IACCaseCreate.inject(rampUsers(1) during (1 minutes))
  )
    .protocols(httpProtocol)
}