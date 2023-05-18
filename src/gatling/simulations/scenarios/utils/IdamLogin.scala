package utils

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object IdamLogin {

  val config: Config = ConfigFactory.load()
  val IdamAPI = Environment.idamAPI
  val ccdScope = "openid profile authorities acr roles openid profile roles"
  val ccdGatewayClientSecret = config.getString("ccdGatewayCS")

  val GetIdamToken =

    exec(http("GetIdamToken")
      .post(IdamAPI + "/o/token?client_id=ccd_gateway&client_secret=" + ccdGatewayClientSecret + "&grant_type=password&scope=" + ccdScope + "&username=#{email}&password=#{password}")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("Content-Length", "0")
      .check(status.is(200))
      .check(jsonPath("$.access_token").saveAs("access_token")))
}