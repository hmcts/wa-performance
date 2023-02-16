package uk.gov.hmcts.wa.scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._

object xuiPrl {

  val baseURL = Environment.xuiBaseURL

  val SearchCase = 

    exec(http("request_1")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_2")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_3")
			.get("/api/globalSearch/services")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_20")
			.get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .exec(http("request_62")
			.post("/api/globalsearch/results")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(ElFileBody("xuiBodies/PRLCaseSearch.json")))

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
			.get("/data/internal/cases/${caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

    .exec(http("XUI_ViewCase_GetWorkAllocationTask")
      .get("/workallocation/case/task/${caseId}")
      .headers(XUIHeaders.xuiMainHeader)
      .check(jsonPath("$[0].id").optional.saveAs("taskId")))

    .pause(Environment.constantthinkTime)

  val AddCaseNumber = 

    exec(http("request_77")
			.get("/workallocation/case/tasks/${caseId}/event/fl401AddCaseNumber/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .exec(http("request_78")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_ViewCase_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_100")
			.get("/data/internal/cases/${caseId}/event-triggers/fl401AddCaseNumber?ignore-warning=false")
			.headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
      .check(jsonPath("$.event_token").saveAs("eventToken")))
    
    .exec(http("request_108")
			.get("/data/internal/profile")
      .headers(XUIHeaders.xuiMainHeader)
			.header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8"))

    .pause(Environment.constantthinkTime)

    .exec(http("request_116")
			.post("/data/case-types/PRLAPPS/validate?pageId=fl401AddCaseNumber1")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(ElFileBody("xuiBodies/PRLAddCaseNumberPage1.json")))

    .exec(http("request_132")
			.get("/workallocation/task/${taskId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .pause(Environment.constantthinkTime)

    .exec(http("request_145")
			.post("/data/cases/${caseId}/events")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(ElFileBody("xuiBodies/PRLAddCaseNumberSubmit.json")))

    .exec(http("request_138")
			.post("/workallocation/task/${taskId}/complete")
			.header("accept", "application/json")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}"))

    .exec(http("XUI_ViewCase_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
    
    .exec(http("XUI_ViewCase_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

    .pause(Environment.constantthinkTime)
}