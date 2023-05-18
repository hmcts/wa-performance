package utils

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object WaitforNextIteration {

  val MinWaitForNextIteration = Environment.minWaitForNextIteration
  val MaxWaitForNextIteration = Environment.maxWaitForNextIteration
    
  val waitforNextIteration = pace(MinWaitForNextIteration seconds, MaxWaitForNextIteration seconds)
}
