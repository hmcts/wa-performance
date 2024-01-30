package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiSscs {

  val baseURL = Environment.xuiBaseURL

  val SearchCase = 

    exec(http("XUI_GlobalSearch_010_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_GlobalSearch_010_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_GlobalSearch_010_Services")
			.get("/api/globalSearch/services")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_GlobalSearch_010_JurisdictionsRead")
			.get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_GlobalSearch_020_Request")
			.post("/api/globalsearch/results")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/FPLCaseSearch.json")))

    .exec(http("XUI_GlobalSearch_020_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
		
		.exec(http("XUI_GlobalSearch_020_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .exec(http("XUI_GlobalSearch_020_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

  val ViewCase = 

    exec(http("XUI_ViewCase_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_ViewCase_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_ViewCase_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_ViewCase_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

    .exec(http("XUI_ViewCase_GetWorkAllocationTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(XUIHeaders.xuiMainHeader)
      .check(jsonPath("$[0].id").saveAs("taskId")))

    .pause(Environment.constantthinkTime)

  val ReviewAdminAction = 

    group("XUI_SSCSReviewAdminAction_Page1") {
      exec(http("XUI_SSCSReviewAdminAction_GetTasks")
        .get("/case/SSCS/Benefit/#{caseId}/trigger/interlocSendToTcw?tid=#{taskId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_SSCSReviewAdminAction_ConfigurationUI")
        .get("/external/configuration-ui/")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_SSCSReviewAdminAction_T&C")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_ConfigJson")
        .get("/assets/config/config.json")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_MonitoringTools")
        .get("/api/monitoring-tools")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_GetTask")
        .get("/workallocation/case/tasks/#{caseId}/event/interlocSendToTcw/caseType/Benefit/jurisdiction/SSCS")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_SSCSReviewAdminAction_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_SSCSReviewAdminAction_Profile")
        .get("/data/internal/profile")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8"))

      .exec(http("XUI_SSCSReviewAdminAction_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/interlocSendToTcw?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_SSCSReviewAdminAction_Page2") {
      exec(http("XUI_SSCSReviewAdminAction_Page2")
        .post("/data/case-types/Benefit/validate?pageId=interlocSendToTcw1.0")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("sscsBodies/SSCSSendToAdmin_Page2.json")))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_SSCSReviewAdminAction_Submit") {
      exec(http("XUI_SSCSReviewAdminAction_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json"))

      .exec(http("XUI_SSCSReviewAdminAction_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_SSCSReviewAdminAction_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("sscsBodies/SSCSSendToAdmin_Submit.json")))

      .exec(http("XUI_SSCSReviewAdminAction_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_SSCSReviewAdminAction_GetJurisdictions")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_SSCSReviewAdminAction_ManageRoleAssignment")
        .post("/api/role-access/roles/manageLabellingRoleAssignment/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_SSCSReviewAdminAction_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }
}