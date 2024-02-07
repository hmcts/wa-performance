package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiEt {

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

  val etVetting = 

    group("XUI_ET_Vetting_Page1") {
      exec(http("XUI_ET_Vetting_GetTasks")
        .get("/cases/case-details/#{caseId}/trigger/et1Vetting/et1Vetting1?tid=#{taskId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ET_Vetting_ConfigurationUI")
        .get("/external/configuration-ui/")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ET_Vetting_T&C")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_Vetting_ConfigJson")
        .get("/assets/config/config.json")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_Vetting_ConfigUI")
        .get("/external/config/ui")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_Vetting_MonitoringTools")
        .get("/api/monitoring-tools")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_Vetting_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_Vetting_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_ET_Vetting_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/et1Vetting?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_Vetting_Profile")
        .get("/data/internal/profile")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page2"){
       exec(http("XUI_ET_Vetting_Page2")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting1")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page1.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page3"){
       exec(http("XUI_ET_Vetting_Page3")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting2")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page2.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page4"){
       exec(http("XUI_ET_Vetting_Page4")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting3")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page3.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page5"){
       exec(http("XUI_ET_Vetting_Page5")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting4")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page4.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page6"){
       exec(http("XUI_ET_Vetting_Page6")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting5")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page5.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page7"){
       exec(http("request_185")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting6")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page6.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page8"){
       exec(http("XUI_ET_Vetting_Page8")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting7")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page7.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page9"){
       exec(http("XUI_ET_Vetting_Page9")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting8")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page8.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page10"){
       exec(http("XUI_ET_Vetting_Page10")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting9")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page9.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page11"){
       exec(http("XUI_ET_Vetting_Page11")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting10")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page10.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page12"){
       exec(http("XUI_ET_Vetting_Page12")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting11")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page11.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page13"){
       exec(http("XUI_ET_Vetting_Page13")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting12")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page12.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page14"){
       exec(http("XUI_ET_Vetting_Page14")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting13")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page13.json")))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Submit") {
      exec(http("XUI_ET_Vetting_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Submit.json")))

      .exec(http("XUI_ET_Vetting_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_ET_Vetting_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(Environment.constantthinkTime)

  val etPreAcceptance =

    group("XUI_ET_PreAcceptance") {
      exec(http("XUI_ET_PreAcceptance_EventTrigger")
        .get("/cases/case-details/#{caseId}/trigger/preAcceptanceCase/preAcceptanceCase1")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ET_PreAcceptance_ConfigurationUI")
        .get("/external/configuration-ui/")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ET_PreAcceptance_T&C")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_PreAcceptance_ConfigJson")
        .get("/assets/config/config.json")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_PreAcceptance_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_PreAcceptance_ConfigUI")
        .get("/external/config/ui")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_PreAcceptance_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ET_PreAcceptance_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/preAcceptanceCase/caseType/ET_EnglandWales/jurisdiction/EMPLOYMENT")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json"))

      .exec(http("XUI_ET_PreAcceptance_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_PreAcceptancePage1") {
      exec(http("XUI_ET_PreAcceptancePage1")
        .get("/data/internal/cases/#{caseId}/event-triggers/preAcceptanceCase?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken2")))

      .exec(http("XUI_ET_PreAcceptancePage1_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_PreAcceptancePage1_Profile")
        .get("/data/internal/profile")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_PreAcceptancePage2") {
      exec(http("XUI_ET_PreAcceptancePage2")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=preAcceptanceCase1")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETPreAcceptance_Page1.json")))

      .exec(http("XUI_ET_PreAcceptancePage2_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_SubmitAcceptance") {
      exec(http("XUI_ET_SubmitAcceptance_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_ET_SubmitAcceptance_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETPreAcceptance_Submit.json")))

      .exec(http("XUI_ET_SubmitAcceptance_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_ET_SubmitAcceptance_GetJurisdictions")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(http("XUI_ET_SubmitAcceptance_RefreshRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*"))
    }
}