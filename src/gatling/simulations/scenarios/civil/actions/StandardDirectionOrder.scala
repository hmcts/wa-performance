package scenarios.civil.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import xui.Headers

object StandardDirectionOrder {
  
  val execute =

    group("XUI_CiviL_JudicialSDO_Page1") {
      exec(http("XUI_CiviL_JudicialSDO_Page1")
        .get("/cases/case-details/#{caseId}/trigger/STANDARD_DIRECTION_ORDER_DJ/STANDARD_DIRECTION_ORDER_DJCaseManagementOrder?tid=#{taskId}")
        .headers(Headers.commonHeader))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_CiviL_JudicialSDO_Page1_WA")
        .get("/workallocation/case/tasks/#{caseId}/event/STANDARD_DIRECTION_ORDER_DJ/caseType/CIVIL/jurisdiction/CIVIL")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("content-type", "application/json"))

      .exec(http("XUI_CiviL_JudicialSDO_Page1_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("content-type", "application/json"))

      .exec(Common.profile)

      .exec(http("XUI_CiviL_JudicialSDO_Page1_GetEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/STANDARD_DIRECTION_ORDER_DJ?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .header("ontent-type", "application/json")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_CiviL_JudicialSDO_Page2") {
      exec(http("XUI_CiviL_JudicialSDO_Page2_Request")
        .post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJCaseManagementOrder")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("xuiBodies/CivilDJPageOne.json"))
        .check(jsonPath("$.data.disposalHearingDisclosureOfDocumentsDJ.date").saveAs("disposalHearingDisclosureOfDocumentsDJDate"))
        .check(jsonPath("$.data.disposalHearingWitnessOfFactDJ.date1").saveAs("disposalHearingWitnessOfFactDJDate1"))
        .check(jsonPath("$.data.disposalHearingWitnessOfFactDJ.date2").saveAs("disposalHearingWitnessOfFactDJDate2"))
        .check(jsonPath("$.data.disposalHearingMedicalEvidenceDJ.date1").saveAs("disposalHearingMedicalEvidenceDJDate1"))
        .check(jsonPath("$.data.disposalHearingQuestionsToExpertsDJ.date").saveAs("disposalHearingQuestionsToExpertsDJDate"))
        .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date1").saveAs("disposalHearingSchedulesOfLossDJDate1"))
        .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date2").saveAs("disposalHearingSchedulesOfLossDJDate2"))
        .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date3").saveAs("disposalHearingSchedulesOfLossDJDate3"))
        .check(jsonPath("$.data.disposalHearingFinalDisposalHearingTimeDJ.date").saveAs("disposalHearingFinalDisposalHearingTimeDJDate")))

      .exec(Common.apiUserDetails)
      }

      .pause(Environment.constantthinkTime)

      .group("XUI_CiviL_JudicialSDO_Page3") {
        exec(http("XUI_CiviL_JudicialSDO_Page3_Request")
          .post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJDisposalHearing")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{XSRFToken}")
          .body(ElFileBody("xuiBodies/CivilDJPageTwo.json"))
          .check(jsonPath("$.data.orderSDODocumentDJCollection[0].value.documentLink.document_url").saveAs("orderSDODocumentDJCollectionURL"))
          .check(jsonPath("$.data.orderSDODocumentDJCollection[0].value.documentLink.document_filename").saveAs("orderSDODocumentDJCollectionName"))
          .check(jsonPath("$.data.orderSDODocumentDJCollection[0].value.documentLink.document_hash").saveAs("orderSDODocumentDJCollectionHash"))
          .check(jsonPath("$.data.orderSDODocumentDJCollection[0].value.documentSize").saveAs("orderSDODocumentDJCollectionDocumentSize"))
          .check(jsonPath("$.data.orderSDODocumentDJCollection[0].value.createdDatetime").saveAs("orderSDODocumentDJCollectioncreatedDatetime"))
          .check(jsonPath("$.data.orderSDODocumentDJCollection[0].id").saveAs("orderSDODocumentDJCollectionId"))
          .check(jsonPath("$.data.disposalHearingDisclosureOfDocumentsDJ.date").saveAs("disposalHearingDisclosureOfDocumentsDJdate"))
          .check(jsonPath("$.data.disposalHearingWitnessOfFactDJ.date1").saveAs("disposalHearingWitnessOfFactDJDate1"))
          .check(jsonPath("$.data.disposalHearingWitnessOfFactDJ.date2").saveAs("disposalHearingWitnessOfFactDJDate2"))
          .check(jsonPath("$.data.disposalHearingMedicalEvidenceDJ.date1").saveAs("disposalHearingMedicalEvidenceDJdate1"))
          .check(jsonPath("$.data.disposalHearingQuestionsToExpertsDJ.date").saveAs("disposalHearingQuestionsToExpertsDJdate"))
          .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date1").saveAs("disposalHearingSchedulesOfLossDJdate1"))
          .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date2").saveAs("disposalHearingSchedulesOfLossDJdate2"))
          .check(jsonPath("$.data.disposalHearingSchedulesOfLossDJ.date3").saveAs("disposalHearingSchedulesOfLossDJdate3"))
          .check(jsonPath("$.data.disposalHearingFinalDisposalHearingTimeDJ.date")saveAs("disposalHearingFinalDisposalHearingTimeDJdate")))

        .exec(Common.apiUserDetails)
      }

      .pause(Environment.constantthinkTime)

      .group("XUI_CiviL_JudicialSDO_Page4") {
        exec(http("XUI_CiviL_JudicialSDO_Page4_Request")
          .post("/data/case-types/CIVIL/validate?pageId=STANDARD_DIRECTION_ORDER_DJOrderPreview")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{XSRFToken}")
          .body(ElFileBody("xuiBodies/CivilDJPageThree.json")))
      }

      .pause(Environment.constantthinkTime)

      .group("XUI_CiviL_JudicialSDO_Submit") {
        exec(http("XUI_CiviL_JudicialSDO_Submit_Request")
          .post("/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("content-type", "application/json")
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "#{XSRFToken}")
          .body(ElFileBody("xuiBodies/CivilDJSubmit.json")))

        .exec(http("XUI_CiviL_JudicialSDO_Submit_WACompleteTask")
          .post("/workallocation/task/#{taskId}/complete")
          .headers(Headers.commonHeader)
          .header("content-type", "application/json")
          .header("accept", "application/json")
          .header("x-xsrf-token", "#{XSRFToken}")
          .body(StringBody("""{}""")))

        .exec(http("XUI_CiviL_JudicialSDO_Submit_GetCase")
          .get("/data/internal/cases/#{caseId}")
          .headers(Headers.commonHeader)
          .header("content-type", "application/json")
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

        .exec(Common.apiUserDetails)
      }
}