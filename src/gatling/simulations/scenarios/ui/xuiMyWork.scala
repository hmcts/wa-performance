package scenarios

import java.text.SimpleDateFormat
import java.util.Date
import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import java.io.{BufferedWriter, FileWriter}

object xuiMyWork {

  val baseURL = Environment.xuiBaseURL
  val IdamURL = Environment.idamURL
  val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
  val feedIACUserData = csv("IACUserData.csv").circular
  val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
  val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular

  val headers_0 = Map(
		"cache-control" -> "no-cache",
		"dnt" -> "1",
		"pragma" -> "no-cache",
		"request-context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"request-id" -> "|vqtOq.Z7tnd",
		"sec-ch-ua" -> """ Not A;Brand";v="99", "Chromium";v="96", "Google Chrome";v="96""",
		"sec-ch-ua-mobile" -> "?0",
		"sec-ch-ua-platform" -> "macOS",
		"sec-fetch-dest" -> "empty",
		"sec-fetch-mode" -> "cors",
		"sec-fetch-site" -> "same-origin")

	val headers_1 = Map(
		"cache-control" -> "no-cache",
		"dnt" -> "1",
		"pragma" -> "no-cache",
		"request-context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"request-id" -> "|vqtOq.zqQv5",
		"sec-ch-ua" -> """ Not A;Brand";v="99", "Chromium";v="96", "Google Chrome";v="96""",
		"sec-ch-ua-mobile" -> "?0",
		"sec-ch-ua-platform" -> "macOS",
		"sec-fetch-dest" -> "empty",
		"sec-fetch-mode" -> "cors",
		"sec-fetch-site" -> "same-origin")

	val headers_2 = Map(
		"cache-control" -> "no-cache",
		"content-type" -> "application/json",
		"dnt" -> "1",
		"origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"pragma" -> "no-cache",
		"request-context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"request-id" -> "|vqtOq.rR//R",
		"sec-ch-ua" -> """ Not A;Brand";v="99", "Chromium";v="96", "Google Chrome";v="96""",
		"sec-ch-ua-mobile" -> "?0",
		"sec-ch-ua-platform" -> "macOS",
		"sec-fetch-dest" -> "empty",
		"sec-fetch-mode" -> "cors",
		"sec-fetch-site" -> "same-origin")

	val headers_3 = Map(
		"cache-control" -> "no-cache",
		"dnt" -> "1",
		"pragma" -> "no-cache",
		"request-context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"request-id" -> "|vqtOq.NNVqO",
		"sec-ch-ua" -> """ Not A;Brand";v="99", "Chromium";v="96", "Google Chrome";v="96""",
		"sec-ch-ua-mobile" -> "?0",
		"sec-ch-ua-platform" -> "macOS",
		"sec-fetch-dest" -> "empty",
		"sec-fetch-mode" -> "cors",
		"sec-fetch-site" -> "same-origin")

	val headers_4 = Map(
		"cache-control" -> "no-cache",
		"content-type" -> "application/json",
		"dnt" -> "1",
		"origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"pragma" -> "no-cache",
		"request-context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"request-id" -> "|vqtOq.trVkp",
		"sec-ch-ua" -> """ Not A;Brand";v="99", "Chromium";v="96", "Google Chrome";v="96""",
		"sec-ch-ua-mobile" -> "?0",
		"sec-ch-ua-platform" -> "macOS",
		"sec-fetch-dest" -> "empty",
		"sec-fetch-mode" -> "cors",
		"sec-fetch-site" -> "same-origin")

  val MyWork = 

    exec(http("XUI_MyWork_Healthcheck")
			.get("/api/healthCheck?path=%2Fwork%2Fmy-work%2Flist")
			.headers(XUIHeaders.xuiMainHeader)) //0

    .exec(http("XUI_MyWork_UserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)) //1

    .exec(http("XUI_MyWork")
			.post("/workallocation2/task")
			.headers(XUIHeaders.xuiMainHeader) //2
      .header("x-xsrf-token", "#{xsrfToken}")
      .header("content-type", "application/json")
			.body(ElFileBody("xuiBodies/MyWork.json")))

    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain("manage-case.perftest.platform.hmcts.net").saveAs("xsrfToken")))

		.pause(Environment.constantthinkTime)

  val AvailableTasks = 

    group("XUI_MyAvailableTasks"){
      exec(http("XUI_MyAvailableTasks_UserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader)) //3

      .exec(http("XUI_MyAvailableTasksRequest")
        .post("/workallocation/task")
        .headers(XUIHeaders.xuiMainHeader) //4
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/MyAvailableTasks.json"))
        // .check(jsonPath("$.tasks[0].id").saveAs("taskId"))
        // .check(jsonPath("$.tasks[0].case_id").saveAs("caseId"))
        )
    }

		.pause(Environment.constantthinkTime)

  val AssignToMeAndGo = 

    group("XUI_AssignAndView"){
      exec(http("XUI_MyAvailableTasks_ClaimTask")
        .post("/workallocation2/task/#{taskId}/claim")
        .headers(XUIHeaders.xuiMainHeader)
        .header("x-xsrf-token", "#{xsrfToken}")
        .header("content-type", "application/json")
        .body(StringBody("{}")))

      .exec(http("XUI_MyAvailableTasks_ClaimTask_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_MyAvailableTasks_ClaimTask_Healthcheck")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F#{caseId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_MyAvailableTasks_ClaimTask_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))
    }
    
    .pause(Environment.constantthinkTime)
}