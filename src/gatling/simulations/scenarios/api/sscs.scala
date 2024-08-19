package scenarios

import java.text.SimpleDateFormat
import java.util.Date
import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import java.io.{BufferedWriter, FileWriter}
import scala.util.Random

object sscs {

  val ccdCreateSSCSCase = 

    exec(_.setAll(
        ("NINumber" -> Common.randomNumber(8)),
        "firstname"  -> ("Perf" + Common.randomString(5)),
        "lastname"  -> ("Test" + Common.randomString(5)),
        "todayDate" -> Common.getDate()
    ))

    .exec(http("API_SSCS_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/SSCS/case-types/Benefit/event-triggers/validAppealCreated/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_SSCS_CreateCase")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/SSCS/case-types/Benefit/cases")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("sscsBodies/SSCSCreateAppeal.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .pause(Environment.constantthinkTime)

  val ccdSendToAdmin = 

    exec(http("API_SSCS_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/SSCS/case-types/Benefit/cases/#{caseId}/event-triggers/sendToAdmin/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_SSCS_SendToAdmin")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/SSCS/case-types/Benefit/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("sscsBodies/SSCSSendToAdmin.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("SSCSSubmittedCaseIds.csv", true))
        try {
          fw.write(session("caseId").as[String] + "\r\n")
        }
        finally fw.close()
        session
    }

    .pause(Environment.constantthinkTime)

  val ccdAddHearing = 

    exec(http("API_SSCS_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/SSCS/case-types/Benefit/cases/#{caseId}/event-triggers/addHearing/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_SSCS_AddHearing")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/SSCS/case-types/Benefit/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("sscsBodies/SSCSAddHearingToday.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .pause(Environment.constantthinkTime)

  val ccdDirectionIssued = 

    exec(http("API_SSCS_GetEventToken")
      .get(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/SSCS/case-types/Benefit/cases/#{caseId}/event-triggers/directionIssued/token")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("API_SSCS_DirectionIssued")
      .post(Environment.ccdDataStoreUrl + "/caseworkers/#{idamId}/jurisdictions/SSCS/case-types/Benefit/cases/#{caseId}/events")
      .header("ServiceAuthorization", "Bearer #{ccd_dataBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type","application/json")
      .body(ElFileBody("sscsBodies/SSCSDirectionIssued.json"))
      .check(jsonPath("$.id").saveAs("caseId")))

    .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("SSCSCasesForCron.csv", true))
        try {
          fw.write(session("caseId").as[String] + "\r\n")
        }
        finally fw.close()
        session
    }

    .pause(Environment.constantthinkTime)

}