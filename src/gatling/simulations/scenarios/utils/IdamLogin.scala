package utils

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object IdamLogin {

  val config: Config = ConfigFactory.load()
  val ccdScope = "openid profile authorities acr roles openid profile roles"
//  val ccdGatewayClientSecret = config.getString("auth.clientSecret")
//  val paybubbleClientSecret = config.getString("auth.paybubbleClientSecret")
  val rdScope = "openid%20profile%20roles%20openid%20roles%20profile%20create-user%20manage-user"

  /*val GetIdamToken =

    exec(http("GetIdamToken")
      .post(Environment.idamAPI + "/o/token?client_id=ccd_gateway&client_secret=" + ccdGatewayClientSecret + "&grant_type=password&scope=" + ccdScope + "&username=#{email}&password=#{password}")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("Content-Length", "0")
      .check(status.is(200))
      .check(jsonPath("$.access_token").saveAs("access_token")))

  val GetIdamTokenPayments =

    exec(http("GetIdamToken")
      .post(Environment.idamAPI + "/o/token?grant_type=password&username=#{email}&password=#{password}&client_id=paybubble&client_secret=" + paybubbleClientSecret + "&redirect_uri=" + Environment.refDataApiURL + "/oauth2redirect&scope=openid%20profile%20roles%20search-user")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("Content-Length", "0")
      .check(status.is(200))
      .check(jsonPath("$.access_token").saveAs("access_tokenPayments")))
      */

}