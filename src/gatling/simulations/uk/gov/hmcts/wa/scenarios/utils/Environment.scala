package uk.gov.hmcts.wa.scenarios.utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Environment {

  val minThinkTime = 10 //10
  val maxThinkTime = 30 //30
  val constantthinkTime = 7 //7

  val minWaitForNextIteration = 60 //120
  val maxWaitForNextIteration = 120 //240
  val HttpProtocol = http

}
