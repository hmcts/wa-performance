package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._
import java.io.{BufferedWriter, FileWriter}

object xuiJudicialTask {

  val baseURL = Environment.xuiBaseURL
  val IdamURL = Environment.idamURL

  val AssignTask = 

    exec(http("request_0")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))
			
    .exec(http("request_2")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.caseActivityOptions))
            
    .exec(http("request_6")
			.get("/workallocation/case/task/${caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("request_7")
			.post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .header("authorization", "Bearer ${bearerToken}")
			.body(StringBody("""{"activity":"view"}""")))
            
    .exec(http("request_18")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("access-control-request-headers", "authorization,content-type")
      .header("access-control-request-method", "POST"))
       
    .exec(http("request_15")
			.post("/workallocation/task/a31ea3b6-4adf-11ed-8e56-caf72b902574/claim")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(StringBody("""{}""")))
            
    .exec(http("request_23")
			.post("/api/role-access/roles/getJudicialUsers")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(StringBody("""{"userIds":["${idamId}"],"services":["CIVIL"]}""")))

  val StandardDirectionOrder = 
           
    exec(http("request_27")
			.get("/cases/case-details/${caseId}/trigger/STANDARD_DIRECTION_ORDER_DJ/STANDARD_DIRECTION_ORDER_DJCaseManagementOrder?tid=a31ea3b6-4adf-11ed-8e56-caf72b902574")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_30")
			.get("/external/configuration-ui/")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_31")
			.get("/assets/config/config.json")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_32")
			.get("/api/configuration?configurationKey=termsAndConditionsEnabled")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_33")
			.get("/external/config/ui")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_34")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))
            
    .exec(http("request_41")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_42")
			.get("/api/monitoring-tools")
			.headers(XUIHeaders.xuiMainHeader))
           
    .exec(http("request_44")
			.get("/workallocation/case/tasks/${caseId}/event/STANDARD_DIRECTION_ORDER_DJ/caseType/CIVIL/jurisdiction/CIVIL")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json")
      .header("content-type" -> "application/json"))

    .exec(http("request_45")
			.options(Environment.ccdGateway + "/activity/cases/0/activity")
			.headers(XUIHeaders.caseActivityOptions))
  
    .exec(http("request_48")
			.get(Environment.ccdGateway + "/activity/cases/0/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .header("authorization", "Bearer ${bearerToken}"))
      
    .exec(http("request_50")
			.get("/data/internal/cases/${caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
      .header("accept", "application/json"))

    .exec(http("request_51")
			.get("/data/internal/profile")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8")
      .header("accept", "application/json"))

    .exec(http("request_52")
			.get("/data/internal/cases/${caseId}/event-triggers/STANDARD_DIRECTION_ORDER_DJ?ignore-warning=false")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
      .header("accept", "application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .exec(http("request_56")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))
            
    .exec(http("request_53")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.caseActivityOptions))

    .exec(http("request_60")
			.post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .header("authorization", "Bearer ${bearerToken}")
			.body(StringBody("""{"activity":"edit"}""")))

    .exec(http("request_61")
			.get(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .header("authorization", "Bearer ${bearerToken}"))
      
		.exec(http("request_71")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.caseActivityOptions))

    .exec(http("request_73")
			.post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJCaseManagementOrder")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("accept", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(ElFileBody("xuiBodies/CivilDJPageOne.json"))
      .check(jsonPath("$.data.trialPersonalInjury.date1").saveAs("trialPersonalInjurydate1"))
      .check(jsonPath("$.data.disposalHearingWitnessOfFactDJ.date3").saveAs("disposalHearingWitnessOfFactDJdate3"))
      .check(jsonPath("$.data.trialHearingDisclosureOfDocumentsDJ.date2").saveAs("date2"))
      .check(jsonPath("$.data.disposalHearingQuestionsToExpertsDJ.date").saveAs("disposalHearingQuestionsToExpertsDJdate"))
      .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date1").saveAs("disposalHearingSchedulesOfLossDJdate1"))
      .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date3").saveAs("disposalHearingSchedulesOfLossDJdate3"))
      .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date2").saveAs("disposalHearingSchedulesOfLossDJdate2"))
      .check(jsonPath("$.data.disposalHearingFinalDisposalHearingDJ.date").saveAs("disposalHearingFinalDisposalHearingDJdate"))
      .check(jsonPath("$.data.disposalHearingNotesDJ.date").saveAs("disposalHearingNotesDJdate")))

    .pause(7)

    .exec(http("request_74")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))
            
    .exec(http("request_82")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.caseActivityOptions))

    .exec(http("request_83")
			.post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .header("authorization", "Bearer ${bearerToken}")
			.body(StringBody("""{"activity":"edit"}""")))

    .exec(http("request_89")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.caseActivityOptions))

    .exec(http("request_115")
			.get(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .header("authorization", "Bearer ${bearerToken}"))
      
    .exec(http("request_118")
			.post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJDisposalHearing")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("accept", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(ElFileBody("xuiBodies/CivilDJPageTwo.json"))
      .check(jsonPath("$.data.orderSDODocumentDJ.document_url").saveAs("orderSDODocumentDJDocumentURL"))
      .check(jsonPath("$.data.orderSDODocumentDJ.document_filename").saveAs("orderSDODocumentDJName"))
      .check(jsonPath("$.data.orderSDODocumentDJ.document_hash").saveAs("orderSDODocumentDJHash"))
      .check(jsonPath("$.data.disposalHearingDisclosureOfDocumentsDJ.date").saveAs("disposalHearingDisclosureOfDocumentsDJdate"))
      .check(jsonPath("$.data.disposalHearingWitnessOfFactDJ.date1").saveAs("disposalHearingWitnessOfFactDJdate1"))
      .check(jsonPath("$.data.disposalHearingWitnessOfFactDJ.date2").saveAs("disposalHearingWitnessOfFactDJdate2"))
      .check(jsonPath("$.data.disposalHearingWitnessOfFactDJ.date3").saveAs("disposalHearingWitnessOfFactDJdate3"))
      .check(jsonPath("$.data.disposalHearingMedicalEvidenceDJ.date1").saveAs("disposalHearingMedicalEvidenceDJdate1"))
      .check(jsonPath("$.data.disposalHearingQuestionsToExpertsDJ.date").saveAs("disposalHearingQuestionsToExpertsDJdate"))
      .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date1").saveAs("disposalHearingSchedulesOfLossDJdate1"))
      .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date3").saveAs("disposalHearingSchedulesOfLossDJdate3"))
      .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date2").saveAs("disposalHearingSchedulesOfLossDJdate2"))
      .check(jsonPath("$.data.disposalHearingFinalDisposalHearingDJ.date").saveAs("disposalHearingFinalDisposalHearingDJdate"))
      .check(jsonPath("$.data.disposalHearingNotesDJ.date").saveAs("disposalHearingNotesDJdate"))
      )

    .pause(7)

    .exec(http("request_119")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))
            
    .exec(http("request_124")
			.post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJOrderPreview")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("accept", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(ElFileBody("xuiBodies/CivilDJPageThree.json")))

    .exec(http("request_134")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(headers_2))
            
    .exec(http("request_136")
			.post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(headers_7)
			.body(RawFileBody("judicialCompleteTask_0136_request.txt")))
            
    .exec(http("request_140")
			.get("/workallocation/task/a31ea3b6-4adf-11ed-8e56-caf72b902574")
			.headers(headers_44))
      
		.exec(http("request_142")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(headers_2)

    .pause(7)

    .exec(http("request_165")
			.post("/workallocation/task/a31ea3b6-4adf-11ed-8e56-caf72b902574/complete")
			.headers(headers_165)
			.body(RawFileBody("judicialCompleteTask_0165_request.txt")))

    .exec(http("request_167")
			.post("/data/cases/${caseId}/events")
			.headers(headers_167)
			.body(RawFileBody("judicialCompleteTask_0167_request.txt")))

    .exec(http("request_170")
			.get("/data/internal/cases/${caseId}")
			.headers(headers_50))

    .exec(http("request_171")
			.post("/workallocation/searchForCompletable")
			.headers(headers_171)
			.body(RawFileBody("judicialCompleteTask_0171_request.txt")))

    .exec(http("request_172")
			.get("/api/user/details")
			.headers(headers_0))

    .exec(http("request_181")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(headers_2))

    .exec(http("request_182")
			.post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(headers_7)
			.body(RawFileBody("judicialCompleteTask_0182_request.txt")))
            
    .exec(http("request_184")
			.options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
			.headers(headers_2))

    .exec(http("request_185")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(headers_0))

}