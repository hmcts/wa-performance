package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.jsonpath.JsonPathCheckType
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

object Common {

  /*======================================================================================
  * Common Utility Functions
  ======================================================================================*/

  val rnd = new Random()
  val now = LocalDate.now()
  val patternDay = DateTimeFormatter.ofPattern("dd")
  val patternMonth = DateTimeFormatter.ofPattern("MM")
  val patternYear = DateTimeFormatter.ofPattern("yyyy")
  val patternReference = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val patternTime = DateTimeFormatter.ofPattern("hh:mm:ss")

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def randomNumber(length: Int) = {
    rnd.alphanumeric.filter(_.isDigit).take(length).mkString
  }

  def getDay(): String = {
    (1 + rnd.nextInt(28)).toString.format(patternDay).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  def getMonth(): String = {
    (1 + rnd.nextInt(12)).toString.format(patternMonth).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  def getYear(): String = {
    (1 + rnd.nextInt(12)).toString.format(patternYear).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  //Date of Birth >= 35 years
  def getDobYear(): String = {
    now.minusYears(35 + rnd.nextInt(70)).format(patternYear)
  }
  //Date of Birth <= 18 years
  def getDobYearChild(): String = {
    now.minusYears(2 + rnd.nextInt(15)).format(patternYear)
  }
  //Date of Death <= 21 years
  def getDodYear(): String = {
    now.minusYears(1 + rnd.nextInt(20)).format(patternYear)
  }

  //CurrentDate
  def getDate(): String = {
    now.format(patternReference)
  }

  //CurrentTime
  def getTime(): String = {
    now.format(patternTime)
  }

  //Saves partyId
  def savePartyId: CheckBuilder[JsonPathCheckType, JsonNode] = jsonPath("$.case_fields[*].value[*].value.party.partyId").saveAs("partyId")

  //Saves user ID
  def saveId: CheckBuilder[JsonPathCheckType, JsonNode] = jsonPath("$.case_fields[*].value[0].id").saveAs("id")

  
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

  val orgDetails =
    exec(http("XUI_Common_000_OrgDetails")
      .get("/api/organisation")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(regex("name|Organisation route error"))
      .check(status.in(200, 304, 401, 403)))


}