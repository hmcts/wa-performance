package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object employment {

  val ccdCreateETCase = 

    exec(_.setAll(  "firstName"  -> ("Perf" + Common.randomString(5)),
                    "lastName"  -> ("Test" + Common.randomString(5)),
                    "dobDay" -> Common.getDay(),
                    "dobMonth" -> Common.getMonth(),
                    "dobYear" -> Common.getDobYear(),
                    "todayDate" -> Common.getDate(),
                    "todayYear" -> Common.getYear()))

    .exec(http("API_ET_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/EMPLOYMENT/case-types/ET_EnglandWales/event-triggers/INITIATE_CASE_DRAFT/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_ET_CreateCase")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/EMPLOYMENT/case-types/ET_EnglandWales/cases")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("etBodies/ETCreateCase.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .pause(Environment.constantthinkTime)

  val ccdETSubmitDraft = 

    exec(http("API_ET_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/EMPLOYMENT/case-types/ET_EnglandWales/cases/#{caseId}/event-triggers/SUBMIT_CASE_DRAFT/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_ET_SubmitDraft")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/EMPLOYMENT/case-types/ET_EnglandWales/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("etBodies/ETSubmitDraft.json")))

    .pause(Environment.constantthinkTime)

//     .exec {
//       session =>
//         val fw = new BufferedWriter(new FileWriter("ETSubmittedCaseIds.csv", true))
//         try {
//           fw.write(session("caseId").as[String] + "\r\n")
//         }
//         finally fw.close()
//         session
//     }


}