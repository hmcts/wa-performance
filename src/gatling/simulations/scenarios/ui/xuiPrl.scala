package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiPrl {

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
			.body(ElFileBody("xuiBodies/PRLCaseSearch.json")))

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
      .check(jsonPath("$[0].id").optional.saveAs("taskId")))

    .pause(Environment.constantthinkTime)

  val AddCaseNumber = 

    exec(http("XUI_AddCaseNumber_GetTasks")
			.get("/workallocation/case/tasks/#{caseId}/event/fl401AddCaseNumber/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .exec(http("XUI_AddCaseNumber_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_AddCaseNumber_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_AddCaseNumber_EventTrigger")
			.get("/data/internal/cases/#{caseId}/event-triggers/fl401AddCaseNumber?ignore-warning=false")
			.headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
      .check(jsonPath("$.event_token").saveAs("eventToken")))
    
    .exec(http("XUI_AddCaseNumber_Profile")
			.get("/data/internal/profile")
      .headers(XUIHeaders.xuiMainHeader)
			.header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8"))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_AddCaseNumber_Page1")
			.post("/data/case-types/PRLAPPS/validate?pageId=fl401AddCaseNumber1")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/PRLAddCaseNumberPage1.json")))

    .exec(http("XUI_AddCaseNumber_Page1GetTask")
			.get("/workallocation/task/#{taskId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_AddCaseNumber_Submit")
			.post("/data/cases/#{caseId}/events")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/PRLAddCaseNumberSubmit.json")))

    .exec(http("XUI_AddCaseNumber_CompleteTask")
			.post("/workallocation/task/#{taskId}/complete")
			.header("accept", "application/json")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}"))

    .exec(http("XUI_AddCaseNumber_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
    
    .exec(http("XUI_AddCaseNumber_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

    .pause(Environment.constantthinkTime)

  val SendToGatekeeper = 

    exec(http("XUI_SendToGatekeeper_EventTrigger")
			.get("/data/internal/cases/#{caseId}/event-triggers/sendToGateKeeper?ignore-warning=false")
			.headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
      .check(jsonPath("$.event_token").saveAs("eventToken")))

    .exec(http("XUI_SendToGatekeeper_GetTask")
			.get("/cases/case-details/#{caseId}/trigger/sendToGateKeeper/sendToGateKeeper1?tid=#{taskId}")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_SendToGatekeeper_ConfigurationUI")
			.get("/external/configuration-ui/")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_SendToGatekeeper_ConfigJson")
			.get("/assets/config/config.json")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_SendToGatekeeper_ConfigUI")
			.get("/external/config/ui")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_SendToGatekeeper_T&C")
			.get("/api/configuration?configurationKey=termsAndConditionsEnabled")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_SendToGatekeeper_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_SendToGatekeeper_GetTasks")
			.get("/workallocation/case/tasks/#{caseId}/event/sendToGateKeeper/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json")
      .header("content-type", "application/json"))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_SendToGatekeeper_Page1")
			.post("/data/case-types/PRLAPPS/validate?pageId=sendToGateKeeper1")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/PRLSendToGatekeeper1.json")))

    .exec(http("XUI_SendToGatekeeper_Page1ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_SendToGatekeeper_Submit")
			.post("/data/cases/#{caseId}/events")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
      .body(ElFileBody("xuiBodies/PRLSendToGatekeeperSubmit.json")))

    .exec(http("XUI_SendToGatekeeper_CompleteTask")
			.post("/workallocation/task/#{taskId}/complete")
			.header("accept", "application/json")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}"))

    .exec(http("XUI_SendToGatekeeper_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
    
    .exec(http("XUI_SendToGatekeeper_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))


}