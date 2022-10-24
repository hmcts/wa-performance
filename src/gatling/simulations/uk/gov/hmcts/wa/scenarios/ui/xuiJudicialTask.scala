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

    group("XUI_JudicialAssignTask") {
      exec(http("XUI_JudicialAssignTask_UserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))
              
      .exec(http("XUI_JudicialAssignTask_GetWACase")
        .get("/workallocation/case/task/${caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_CaseActivity_Post")
        .post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("sec-fetch-site", "same-site")
        .header("authorization", "Bearer ${bearerToken}")
        .body(StringBody("""{"activity":"view"}""")))
              
      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("access-control-request-headers", "authorization,content-type")
        .header("access-control-request-method", "POST"))
        
      .exec(http("XUI_JudicialAssignTask_ClaimTask")
        .post("/workallocation/task/${taskId}/claim")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{}""")))
              
      .exec(http("XUI_JudicialAssignTask_GetJudicialUsers")
        .post("/api/role-access/roles/getJudicialUsers")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"userIds":["${idamId}"],"services":["CIVIL"]}""")))
    }

    .pause(Environment.constantthinkTime)

  val StandardDirectionOrder = 
    
    group("XUI_JudicialSDO_Page1") {
      exec(http("XUI_JudicialSDO_Page1")
        .get("/cases/case-details/${caseId}/trigger/STANDARD_DIRECTION_ORDER_DJ/STANDARD_DIRECTION_ORDER_DJCaseManagementOrder?tid=${taskId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_JudicialSDO_Page1_ConfigurationUI")
        .get("/external/configuration-ui/")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_JudicialSDO_Page1_ConfigJson")
        .get("/assets/config/config.json")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_JudicialSDO_Page1_T&C")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_JudicialSDO_Page1_ConfigUI")
        .get("/external/config/ui")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_JudicialSDO_Page1_ApiUserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
              
      .exec(http("XUI_JudicialSDO_Page1_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_JudicialSDO_Page1_MonitoringTools")
        .get("/api/monitoring-tools")
        .headers(XUIHeaders.xuiMainHeader))
            
      .exec(http("XUI_JudicialSDO_Page1_WA")
        .get("/workallocation/case/tasks/${caseId}/event/STANDARD_DIRECTION_ORDER_DJ/caseType/CIVIL/jurisdiction/CIVIL")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json")
        .header("content-type", "application/json"))

      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/0/activity")
        .headers(XUIHeaders.caseActivityOptions))
    
      .exec(http("XUI_CaseActivity_Get")
        .get(Environment.ccdGateway + "/activity/cases/0/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("authorization", "Bearer ${bearerToken}"))
        
      .exec(http("XUI_JudicialSDO_Page1_GetCase")
        .get("/data/internal/cases/${caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("accept", "application/json"))

      .exec(http("XUI_JudicialSDO_Page1_Profile")
        .get("/data/internal/profile")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8")
        .header("accept", "application/json"))

      .exec(http("XUI_JudicialSDO_Page1_GetEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/STANDARD_DIRECTION_ORDER_DJ?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .header("accept", "application/json")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(http("XUI_JudicialSDO_Page1_ApiUserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
              
      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))

      .exec(http("XUI_CaseActivity_Post")
        .post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("content-type", "application/json")
        .header("authorization", "Bearer ${bearerToken}")
        .body(StringBody("""{"activity":"edit"}""")))

      .exec(http("XUI_CaseActivity_Get")
        .get(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("authorization", "Bearer ${bearerToken}"))
        
      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_JudicialSDO_Page2") {
      exec(http("XUI_JudicialSDO_Page2_Request")
        .post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJCaseManagementOrder")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
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
    
      .exec(http("XUI_JudicialSDO_Page2_ApiUserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
              
      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))

      .exec(http("XUI_CaseActivity_Post")
        .post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("content-type", "application/json")
        .header("authorization", "Bearer ${bearerToken}")
        .body(StringBody("""{"activity":"edit"}""")))

      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))

      .exec(http("XUI_CaseActivity_Get")
        .get(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("authorization", "Bearer ${bearerToken}"))
    }

    .pause(Environment.constantthinkTime)
        
    .group("XUI_JudicialSDO_Page3") {
      exec(http("XUI_JudicialSDO_Page3_Request")
        .post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJDisposalHearing")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
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
        .check(jsonPath("$.data.disposalHearingNotesDJ.date").saveAs("disposalHearingNotesDJdate")))

      .exec(http("XUI_JudicialSDO_Page3_ApiUserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
    }
 
    .group("XUI_JudicialSDO_Page4") {     
      exec(http("XUI_JudicialSDO_Page4_Request")
        .post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJOrderPreview")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(ElFileBody("xuiBodies/CivilDJPageThree.json")))

      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))

      .exec(http("XUI_CaseActivity_Post")
        .post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("content-type", "application/json")
        .header("authorization", "Bearer ${bearerToken}")
        .body(StringBody("""{"activity":"edit"}""")))

      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))

      .exec(http("XUI_CaseActivity_Get")
        .get(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("authorization", "Bearer ${bearerToken}"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_JudicialSDO_Submit") {
      exec(http("XUI_JudicialSDO_Submit_Request")
        .post("/data/cases/${caseId}/events")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(ElFileBody("xuiBodies/CivilDJSubmit.json")))
        
      .exec(http("XUI_JudicialSDO_Submit_WACompleteTask")
        .post("/workallocation/task/${taskId}/complete")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{}""")))

      .exec(http("XUI_JudicialSDO_Submit_GetCase")
        .get("/data/internal/cases/${caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_JudicialSDO_Submit_WASearchForCompletable")
        .post("/workallocation/searchForCompletable")
        .header("content-type", "application/json")
        .header("accept", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"STANDARD_DIRECTION_ORDER_DJ","jurisdiction":"CIVIL","caseTypeId":"CIVIL"}}""")))

      .exec(http("XUI_JudicialSDO_Submit_ApiUserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))

      .exec(http("XUI_CaseActivity_Post")
        .post(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("content-type", "application/json")
        .header("authorization", "Bearer ${bearerToken}")
        .body(StringBody("""{"activity":"edit"}""")))

      .exec(http("XUI_CaseActivity_Options")
        .options(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.caseActivityOptions))

      .exec(http("XUI_CaseActivity_Get")
        .get(Environment.ccdGateway + "/activity/cases/${caseId}/activity")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("sec-fetch-site", "same-site")
        .header("authorization", "Bearer ${bearerToken}"))

      .exec(http("XUI_JudicialSDO_Submit_WAGGetJurisdictions")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(XUIHeaders.xuiMainHeader))
    }
}