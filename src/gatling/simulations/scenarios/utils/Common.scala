package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Common {

  /*======================================================================================
  * Common Utility Functions
  ======================================================================================*/

//  val rnd = new Random()
//
//  def randomString(length: Int) = {
//    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
//  }
//
//  def randomNumber(length: Int) = {
//    rnd.alphanumeric.filter(_.isDigit).take(length).mkString
//  }

  /*======================================================================================
  * Common XUI Calls
  ======================================================================================*/

  val postcodeFeeder = csv("postcodes.csv").random

  val postcodeLookup =
    feed(postcodeFeeder)
      .exec(http("XUI_Common_PostcodeLookup")
        .get("/api/addresses?postcode=#{postcode}")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json")
        .check(jsonPath("$.header.totalresults").ofType[Int].gt(0))
        .check(regex(""""(?:BUILDING|ORGANISATION)_.+" : "(.+?)",(?s).*?"(?:DEPENDENT_LOCALITY|THOROUGHFARE_NAME)" : "(.+?)",.*?"POST_TOWN" : "(.+?)",.*?"POSTCODE" : "(.+?)"""")
          .ofType[(String, String, String, String)].findRandom.saveAs("addressLines")))
  
  val configurationui =
    exec(http("XUI_Common_ConfigurationUI")
      .get("/external/configuration-ui/")
      .headers(Headers.xuiMainHeader)
      .header("accept", "*/*")
      .check(substring("ccdGatewayUrl")))

  val configJson =
    exec(http("XUI_Common_ConfigJson")
      .get("/assets/config/config.json")
      .header("accept", "application/json, text/plain, */*")
      .check(substring("caseEditorConfig")))

  val TsAndCs =
    exec(http("XUI_Common_TsAndCs")
      .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
      .headers(Headers.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(substring("false")))

  val userDetails =
    exec(http("XUI_Common_UserDetails")
      .get("/api/user/details?refreshRoleAssignments=undefined")
      .headers(Headers.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

  val configUI =
    exec(http("XUI_Common_ConfigUI")
      .get("/external/config/ui")
      .headers(Headers.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(substring("ccdGatewayUrl")))

  val isAuthenticated =
    exec(http("XUI_Common_IsAuthenticated")
      .get("/auth/isAuthenticated")
      .headers(Headers.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(regex("true|false")))

  val profile =
    exec(http("XUI_Common_Profile")
      .get("/data/internal/profile")
      .headers(Headers.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8")
      .check(jsonPath("$.user.idam.id").notNull))

  val monitoringTools =
    exec(http("XUI_Common_MonitoringTools")
      .get("/api/monitoring-tools")
      .headers(Headers.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(jsonPath("$.key").notNull))

  val waJurisdictions = 
    exec(http("XUI_Common_WAJurisdictionsGet")
      .get("/api/wa-supported-jurisdiction/get")
			.headers(Headers.commonHeader)
      .check(substring("[")))

  val manageLabellingRoleAssignment =
    exec(http("XUI_Common_ManageLabellingRoleAssignments")
      .post("/api/role-access/roles/manageLabellingRoleAssignment/#{caseId}")
      .headers(Headers.commonHeader)
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(StringBody("{}"))
      .check(status.is(204))) 
      //No response body is returned, therefore no substring check is possible

  val apiUserDetails =
    exec(http("XUI_Common_ApiUserDetails")
			.get("/api/user/details")
			.headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*"))

  val waSupportedJurisdictions =
    exec(http("XUI_Common_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*"))
}