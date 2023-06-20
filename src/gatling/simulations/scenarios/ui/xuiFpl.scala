package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiFpl {

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



}