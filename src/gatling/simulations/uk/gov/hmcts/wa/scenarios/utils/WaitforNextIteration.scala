package uk.gov.hmcts.wa.scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils.Environment

object WaitforNextIteration {

  val MinWaitForNextIteration = Environment.minWaitForNextIteration
  val MaxWaitForNextIteration = Environment.maxWaitForNextIteration
    
  val waitforNextIteration = pace(MinWaitForNextIteration seconds, MaxWaitForNextIteration seconds)
}
