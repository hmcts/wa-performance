package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._
import java.io.{BufferedWriter, FileWriter}

object ccddatastore {

val config: Config = ConfigFactory.load()

val IdamURL = Environment.idamURL
val IdamAPI = Environment.idamAPI
val CCDEnvurl = Environment.ccdEnvurl
val s2sUrl = Environment.s2sUrl
val ccdRedirectUri = "https://ccd-data-store-api-perftest.service.core-compute-perftest.internal/oauth2redirect"
val ccdDataStoreUrl = "http://ccd-data-store-api-perftest.service.core-compute-perftest.internal"
val escaseDataUrl = "https://ccd-api-gateway-web-perftest.service.core-compute-perftest.internal"
val dmStoreUrl = "http://dm-store-perftest.service.core-compute-perftest.internal"
val ccdClientId = "ccd_gateway"
val ccdScope = "openid profile authorities acr roles openid profile roles"
val ccdGatewayClientSecret = config.getString("ccdGatewayCS")
val feedIACUserData = csv("IACUserData.csv").circular

val ccdIdamLogin =

  feed(feedIACUserData)

  .exec(http("GetS2SToken")
    .post(s2sUrl + "/testing-support/lease")
    .header("Content-Type", "application/json")
    .body(StringBody("{\"microservice\":\"ccd_data\"}"))
    .check(bodyString.saveAs("bearerToken")))
    .exitHereIfFailed

  .exec(http("OIDC01_Authenticate")
    .post(IdamAPI + "/authenticate")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .formParam("username", "${IACUserName}")
    .formParam("password", "${IACUserPassword}")
    .formParam("redirectUri", ccdRedirectUri)
    .formParam("originIp", "0:0:0:0:0:0:0:1")
    .check(status is 200)
    .check(headerRegex("Set-Cookie", "Idam.Session=(.*)").saveAs("authCookie")))
    .exitHereIfFailed

  .exec(http("OIDC02_Authorize_CCD")
    .post(IdamAPI + "/o/authorize?response_type=code&client_id=" + ccdClientId + "&redirect_uri=" + ccdRedirectUri + "&scope=" + ccdScope).disableFollowRedirect
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Cookie", "Idam.Session=${authCookie}")
    .header("Content-Length", "0")
    .check(status is 302)
    .check(headerRegex("Location", "code=(.*)&client_id").saveAs("code")))
    .exitHereIfFailed

  .exec(http("OIDC03_Token_CCD")
    .post(IdamAPI + "/o/token?grant_type=authorization_code&code=${code}&client_id=" + ccdClientId +"&redirect_uri=" + ccdRedirectUri + "&client_secret=" + ccdGatewayClientSecret)
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Content-Length", "0")
    .check(status is 200)
    .check(jsonPath("$.access_token").saveAs("access_token")))
    .exitHereIfFailed

  val ccdCreateCase = 

    exec(http("API_IAC_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/event-triggers/startAppeal/token")
      .header("ServiceAuthorization", "Bearer ${bearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_IAC_CreateCase")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/cases")
      .header("ServiceAuthorization", "Bearer ${bearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("IACCreateCase.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .pause(Environment.constantthinkTime)

  val ccdSubmitAppeal = 

    exec(http("API_IAC_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/cases/${caseId}/event-triggers/submitAppeal/token")
      .header("ServiceAuthorization", "Bearer ${bearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_IAC_SubmitAppeal")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/cases/${caseId}/events")
      .header("ServiceAuthorization", "Bearer ${bearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("IACSubmitAppeal.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("IACSubmittedCaseIds.csv", true))
        try {
          fw.write(session("caseId").as[String] + "\r\n")
        }
        finally fw.close()
        session
    }

    .pause(Environment.constantthinkTime)
}