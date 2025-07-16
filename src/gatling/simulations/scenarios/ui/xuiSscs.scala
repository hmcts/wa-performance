package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiSscs {

  val baseURL = Environment.xuiBaseURL

  val SearchCase = 

    group("XUI_GlobalSearch_Request") {
      exec(http("XUI_GlobalSearch_010_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_GlobalSearch_010_ApiUserDetails")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_GlobalSearch_010_Services")
        .get("/api/globalSearch/services")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_GlobalSearch_010_JurisdictionsRead")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .pause(Environment.constantthinkTime)

      .exec(http("XUI_GlobalSearch_020_Request")
        .post("/api/globalsearch/results")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/FPLCaseSearch.json")))

      .exec(http("XUI_GlobalSearch_020_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
      
      .exec(http("XUI_GlobalSearch_020_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .exec(http("XUI_GlobalSearch_020_ApiUserDetails")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

  val ViewCase = 

    exec(http("XUI_ViewCase_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(Headers.xuiMainHeader))

    .exec(http("XUI_ViewCase_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(Headers.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_ViewCase_ApiUserDetails")
			.get("/api/user/details")
			.headers(Headers.xuiMainHeader))

    .exec(http("XUI_ViewCase_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(Headers.xuiMainHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

    .exec(http("XUI_ViewCase_GetWorkAllocationTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(Headers.xuiMainHeader)
      .check(jsonPath("$[0].id").saveAs("taskId")))

    .pause(Environment.constantthinkTime)

  val ReviewAdminAction = 

    group("XUI_SSCSReviewAdminAction_Page1") {
      exec(http("XUI_SSCSReviewAdminAction_GetTasks")
        .get("/case/SSCS/Benefit/#{caseId}/trigger/interlocSendToTcw?tid=#{taskId}")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_SSCSReviewAdminAction_ConfigurationUI")
        .get("/external/configuration-ui/")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_SSCSReviewAdminAction_T&C")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_ConfigJson")
        .get("/assets/config/config.json")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_MonitoringTools")
        .get("/api/monitoring-tools")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_GetTask")
        .get("/workallocation/case/tasks/#{caseId}/event/interlocSendToTcw/caseType/Benefit/jurisdiction/SSCS")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_SSCSReviewAdminAction_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_SSCSReviewAdminAction_Profile")
        .get("/data/internal/profile")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8"))

      .exec(http("XUI_SSCSReviewAdminAction_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/interlocSendToTcw?ignore-warning=false")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_SSCSReviewAdminAction_Page2") {
      exec(http("XUI_SSCSReviewAdminAction_Page2")
        .post("/data/case-types/Benefit/validate?pageId=interlocSendToTcw1.0")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("sscsBodies/SSCSSendToAdmin_Page2.json")))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_SSCSReviewAdminAction_Submit") {
      exec(http("XUI_SSCSReviewAdminAction_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json"))

      .exec(http("XUI_SSCSReviewAdminAction_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_SSCSReviewAdminAction_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("sscsBodies/SSCSSendToAdmin_Submit.json")))

      .exec(http("XUI_SSCSReviewAdminAction_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_SSCSReviewAdminAction_GetJurisdictions")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_ManageRoleAssignment")
        .post("/api/role-access/roles/manageLabellingRoleAssignment/#{caseId}")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(Headers.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }
}