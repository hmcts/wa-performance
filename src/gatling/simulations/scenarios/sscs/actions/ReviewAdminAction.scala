package scenarios.sscs.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment}
import xui.Headers

object ReviewAdminAction {
  
  val execute =
    
    group("XUI_SSCSReviewAdminAction_Page1") {
      exec(http("XUI_SSCSReviewAdminAction_GetTasks")
        .get("/case/SSCS/Benefit/#{caseId}/trigger/interlocSendToTcw?tid=#{taskId}")
        .headers(Headers.commonHeader))

      .exec(http("XUI_SSCSReviewAdminAction_ConfigurationUI")
        .get("/external/configuration-ui/")
        .headers(Headers.commonHeader))

      .exec(http("XUI_SSCSReviewAdminAction_T&C")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_ConfigJson")
        .get("/assets/config/config.json")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_MonitoringTools")
        .get("/api/monitoring-tools")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_GetTask")
        .get("/workallocation/case/tasks/#{caseId}/event/interlocSendToTcw/caseType/Benefit/jurisdiction/SSCS")
        .headers(Headers.commonHeader))

      .exec(http("XUI_SSCSReviewAdminAction_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_SSCSReviewAdminAction_Profile")
        .get("/data/internal/profile")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8"))

      .exec(http("XUI_SSCSReviewAdminAction_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/interlocSendToTcw?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_SSCSReviewAdminAction_Page2") {
      exec(http("XUI_SSCSReviewAdminAction_Page2")
        .post("/data/case-types/Benefit/validate?pageId=interlocSendToTcw1.0")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("sscsBodies/SSCSSendToAdmin_Page2.json")))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_SSCSReviewAdminAction_Submit") {
      exec(http("XUI_SSCSReviewAdminAction_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json"))

      .exec(http("XUI_SSCSReviewAdminAction_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_SSCSReviewAdminAction_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("sscsBodies/SSCSSendToAdmin_Submit.json")))

      .exec(http("XUI_SSCSReviewAdminAction_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_SSCSReviewAdminAction_GetJurisdictions")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_ManageRoleAssignment")
        .post("/api/role-access/roles/manageLabellingRoleAssignment/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))
    }
}