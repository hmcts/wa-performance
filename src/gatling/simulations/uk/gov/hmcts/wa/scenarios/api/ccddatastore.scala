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
  val ccdScope = "openid profile authorities acr roles openid profile roles" //perftest
  // val ccdScope = "openid profile authorities acr roles" //aat
  val ccdGatewayClientSecret = config.getString("ccdGatewayCS")

  val ccdCreateIACCase = 

    exec(_.setAll(  "firstName"  -> ("Perf" + Common.randomString(5)),
                    "lastName"  -> ("Test" + Common.randomString(5)),
                    "dobDay" -> Common.getDay(),
                    "dobMonth" -> Common.getMonth(),
                    "dobYear" -> Common.getDobYear(),
                    "todayDate" -> Common.getDate()))

    .exec(http("API_IAC_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/event-triggers/startAppeal/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_IAC_CreateCase")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/cases")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("iacBodies/IACCreateCase.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .pause(Environment.constantthinkTime)

  val ccdIACSubmitAppeal = 

    exec(http("API_IAC_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/cases/${caseId}/event-triggers/submitAppeal/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_IAC_SubmitAppeal")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/cases/${caseId}/events")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("iacBodies/IACSubmitAppeal.json")))
      // .check(jsonPath("$.id").saveAs("caseId")))

    .pause(Environment.constantthinkTime)

  val ccdIACRequestHomeOfficeData =

    exec(http("API_IAC_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/cases/${caseId}/event-triggers/requestHomeOfficeData/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken1")))

    .exec(http("API_IAC_RequestHomeOfficeData")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/${IACJurisdiction}/case-types/${IACCaseType}/cases/${caseId}/events")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("iacBodies/IACRequestHomeOfficeData.json"))
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

  val civilCreateCase = 

    exec(http("API_Civil_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/event-triggers/CREATE_CLAIM/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken1")))

    .exec(http("API_Civil_CreateUnspecifiedClaim")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("civilBodies/CreateSpecifiedClaim.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("CivilCreatedCaseIds.csv", true))
        try {
          fw.write(session("caseId").as[String] + "\r\n")
        }
        finally fw.close()
        session
    }

    .pause(Environment.constantthinkTime)

  val civilCreateCaseGA = 

    exec(http("API_Civil_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/event-triggers/CREATE_CLAIM/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken1")))

    .exec(http("API_Civil_CreateUnspecifiedClaim")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("civilBodies/CreateSpecifiedClaimGA.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("CivilCreatedCaseIdsGA.csv", true))
        try {
          fw.write(session("caseId").as[String] + "\r\n")
        }
        finally fw.close()
        session
    }

    .pause(Environment.constantthinkTime)

  val civilNotifyClaim = 

    exec(http("API_Civil_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/${caseId}/event-triggers/NOTIFY_DEFENDANT_OF_CLAIM/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken2")))

    .exec(http("API_Civil_NotifyClaim")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/${caseId}/events")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("civilBodies/NotifyClaim.json")))

    .pause(Environment.constantthinkTime)

  val civilNotifyClaimDetails = 

    exec(http("API_Civil_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/${caseId}/event-triggers/NOTIFY_DEFENDANT_OF_CLAIM_DETAILS/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken3")))

    .exec(http("API_Civil_NotifyClaimDetails")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/${caseId}/events")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("civilBodies/NotifyClaimDetails.json")))

    .pause(Environment.constantthinkTime)

  val civilUpdateDate = 

    exec(http("API_Civil_UpdateDate")
      .put("http://civil-service-perftest.service.core-compute-perftest.internal/testing-support/case/${caseId}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-type", "application/json")
      .body(ElFileBody("civilBodies/UpdateClaimDate.json")))

    .pause(Environment.constantthinkTime)

  val civilRequestDefaultJudgement = 

    exec(http("API_Civil_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/${caseId}/event-triggers/DEFAULT_JUDGEMENT/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken4")))

    .exec(http("API_Civil_RequestDefaultJudgement")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/${caseId}/events")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("civilBodies/RequestDefaultJudgement.json")))

    .pause(Environment.constantthinkTime)

  val civilCreateGeneralApplication = 

    exec(http("API_Civil_GetEventToken")
      .get(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/${caseId}/event-triggers/DEFAULT_JUDGEMENT/token")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken4")))

    .exec(http("API_Civil_RequestDefaultJudgement")
      .post(ccdDataStoreUrl + "/caseworkers/${idamId}/jurisdictions/CIVIL/case-types/CIVIL/cases/${caseId}/events")
      .header("ServiceAuthorization", "Bearer ${ccd_dataBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("civilBodies/RequestDefaultJudgement.json")))

    .pause(Environment.constantthinkTime)
}