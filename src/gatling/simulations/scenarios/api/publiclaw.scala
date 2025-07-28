package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object publiclaw {

  val ccdCreateFPLCase = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/event-triggers/openCase/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_CreateCase")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLCreateCase.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .pause(Environment.constantthinkTime)

    // .exec {
    //   session =>
    //     val fw = new BufferedWriter(new FileWriter("FPLSubmittedCaseIds.csv", true))
    //     try {
    //       fw.write(session("caseId").as[String] + "\r\n")
    //     }
    //     finally fw.close()
    //     session
    // }

  val ccdFPLOrdersNeeded = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/ordersNeeded/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_OrdersNeeded")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLOrdersNeeded.json")))

    .pause(Environment.constantthinkTime)

  val ccdFPLHearingNeeded = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/hearingNeeded/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_HearingNeeded")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLHearingNeeded.json")))

    .pause(Environment.constantthinkTime)

  val ccdFPLEnterGrounds = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/enterGrounds/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_EnterGrounds")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLEnterGrounds.json")))

    .pause(Environment.constantthinkTime)

  val ccdFPLEnterLocalAuthority = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/enterLocalAuthority/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_EnterLocalAuthority")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLEnterLocalAuthority.json")))

    .pause(Environment.constantthinkTime)

  val ccdFPLEnterChildren = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/enterChildren/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_EnterChildren")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLEnterChildren.json")))

    .pause(Environment.constantthinkTime)

  val ccdFPLEnterRespondents = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/enterRespondents/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_EnterRespondents")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLEnterRespondents.json")))

    .pause(Environment.constantthinkTime)

  val ccdFPLOtherProposal = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/otherProposal/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_OtherProposal")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLOtherProposal.json")))

    .pause(Environment.constantthinkTime)

  val ccdFPLSubmitApplication = 

    exec(http("API_FPL_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/submitApplication/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_FPL_SubmitApplication")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("fplBodies/FPLSubmitApplication.json")))

    .pause(25)

  val ccdSendMessage = 

    tryMax(2) {
      exec(http("API_FPL_GetEventToken")
        .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/event-triggers/messageJudgeOrLegalAdviser/token")
        .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
        .header("Authorization", "Bearer #{access_token}")
        .header("Content-Type","application/json")
        .check(jsonPath("$.token").saveAs("eventToken")))
    }

    .tryMax(2) {
      exec(http("API_FPL_SendMessage")
        .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/#{caseId}/events")
        .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
        .header("Authorization", "Bearer #{access_token}")
        .header("Content-Type","application/json")
        .body(ElFileBody("fplBodies/FPLSendMessage.json")))
    }

    // .exec {
    //   session =>
    //     val fw = new BufferedWriter(new FileWriter("FPLSubmittedCaseIds.csv", true))
    //     try {
    //       fw.write(session("caseId").as[String] + "\r\n")
    //     }
    //     finally fw.close()
    //     session
    // }

    .pause(Environment.constantthinkTime)

}