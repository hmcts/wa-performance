package utils

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object S2S {

  val config: Config = ConfigFactory.load()

  //microservice is a string defined in the Simulation and passed into the body below
  def s2s(microservice: String) = {

    exec(http("GetS2SToken")
      .post(Environment.s2sUrl + "/testing-support/lease")
      .header("Content-Type", "application/json")
      .body(StringBody(s"""{"microservice":"${microservice}"}"""))
      .check(bodyString.saveAs(s"${microservice}BearerToken")))
      .exitHereIfFailed

  }
}