package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._
import java.io.{BufferedWriter, FileWriter}

object xuiwa {

  val baseURL = Environment.xuiBaseURL
  val IdamURL = Environment.idamURL
  val taskListFeeder = csv("WA_TaskList.csv").circular
  val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
  val feedIACUserData = csv("IACUserData.csv").circular
  val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
  val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular
  val caseListFeeder = csv("WA_CaseList.csv").circular
  val feedCompleteTaskListFeeder = csv("WA_TasksToComplete.csv")
  val feedAssignTaskListFeeder = csv("WA_TasksToAssign.csv")
  val feedStaticTasksFeeder = csv("WA_StaticTasks.csv").random

  val manageCasesHomePage =

    group("XUI_Homepage"){
      exec(http("XUI_010_005_Homepage")
        .get(baseURL + "/")
        .headers(XUIHeaders.headers_0)
        .check(status.in(200,304))).exitHereIfFailed
    
      .exec(http("XUI_010_010_HomepageConfigUI")
        .get(baseURL + "/external/configuration-ui")
        .headers(XUIHeaders.headers_1))
    
      .exec(http("XUI_010_015_HomepageConfigJson")
        .get(baseURL + "/assets/config/config.json")
        .headers(XUIHeaders.headers_1))
    
      .exec(http("XUI_010_020_HomepageTCEnabled")
        .get(baseURL + "/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.headers_1))
    
      .exec(http("XUI_010_025_HomepageIsAuthenticated")
        .get(baseURL + "/auth/isAuthenticated")
        .headers(XUIHeaders.headers_1))
    
      .exec(http("XUI_010_030_AuthLogin")
        .get(baseURL + "/auth/login")
        .headers(XUIHeaders.headers_4)
        .check(css("input[name='_csrf']", "value").saveAs("csrfToken"))
        .check(regex("oauth2/callback&state=(.*)&nonce").saveAs("state"))
        .check(regex("&nonce=(.*)&response_type").saveAs("nonce")))
    }

  val manageCasesLoginSenior = 

    feed(feedWASeniorUserData)

    .group("XUI_Login") {
      exec(http("XUI_020_005_SignIn")
        //.post(IdamUrl + "/login?response_type=code&client_id=xuiwebapp&redirect_uri=" + baseURL + "/oauth2/callback&scope=profile%20openid%20roles%20manage-user%20create-user")
        // .post(IdamUrl + "/login?response_type=code&redirect_uri=" + baseURL + "%2Foauth2%2Fcallback&scope=profile%20openid%20roles%20manage-user%20create-user&state=${state}&client_id=xuiwebapp")
        .post(IdamURL + "/login?client_id=xuiwebapp&redirect_uri=" + baseURL + "/oauth2/callback&state=${state}&nonce=${nonce}&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user&prompt=")
        .formParam("username", "${email}")
        .formParam("password", "${password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "false")
        .formParam("_csrf", "${csrfToken}")
        .headers(XUIHeaders.headers_login_submit)
        .check(status.in(200, 304, 302))).exitHereIfFailed

      .exec(http("XUI_020_010_Homepage")
        .get(baseURL + "/external/config/ui")
        .headers(XUIHeaders.headers_0)
        .check(status.in(200,304)))

      .exec(http("XUI_020_015_SignInTCEnabled")
        .get(baseURL + "/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.headers_38)
        .check(status.in(200, 304)))

      .repeat(1, "count") {
        exec(http("XUI_020_020_AcceptT&CAccessJurisdictions${count}")
          .get(baseURL + "/aggregated/caseworkers/:uid/jurisdictions?access=read")
          .headers(XUIHeaders.headers_access_read)
          .check(status.in(200, 304, 302)))
      }

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain("manage-case.perftest.platform.hmcts.net").saveAs("xsrfToken")))
    }

    .pause(Environment.constantthinkTime)

  val openTaskManager =

    exec(http("XUI_OpenTaskManager_005")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.headers_tm0))

    .exec(http("XUI_OpenTaskManager_010")
			.get("/api/healthCheck?path=%2Ftasks%2Ftask-manager")
			.headers(XUIHeaders.headers_tm0))

    // .exec(http("XUI_OpenTaskManager_015")
		// 	.get("/api/healthCheck?path=%2Ftasks%2Ftask-manager")
		// 	.headers(XUIHeaders.headers_tm0))

    .exec(http("XUI_OpenTaskManager_015")
			.get("/workallocation/location")
			.headers(XUIHeaders.headers_tm0))

    .exec(http("XUI_OpenTaskManager_020")
			.post("/workallocation/task")
			.headers(XUIHeaders.headers_tm10)
      .body(StringBody("""{"searchRequest":{"search_parameters":[{"key":"location","operator":"IN","values":["231596","698118","198444","386417","512401","227101","562808","765324"]},{"key":"user","operator":"IN","values":[]}],"sorting_parameters":[{"sort_by":"dueDate","sort_order":"asc"}]},"view":"TaskManager"}""")))

    .pause(Environment.constantthinkTime)

  val assignTask =

    feed(feedAssignTaskListFeeder)

    .exec(http("XUI_OpenTask")
			.get("/workallocation/task/${taskId}")
			.headers(XUIHeaders.headers_tm0))

    // .exec(http("request_11")
		// 	.get("/workallocation/location")
		// 	.headers(XUIHeaders.headers_tm0))

    // .exec(http("request_13")
		// 	.get("/api/monitoring-tools")
		// 	.headers(XUIHeaders.headers_tm0))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_AssignTask_005")
			.post("/workallocation/task/${taskId}/assign")
			.headers(XUIHeaders.headers_assign33)
			.body(StringBody("""{"userId":"${idamId}"}""")))

    .exec(http("XUI_AssignTask_010")
			.get("/api/healthCheck?path=%2Ftasks%2Ftask-manager%23manage_${taskId}")
			.headers(XUIHeaders.headers_assign35))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_OpenTaskManager_005")
			.get("/workallocation/location")
			.headers(XUIHeaders.headers_assign35))

    .exec(http("XUI_OpenTaskManager_020")
			.post("/workallocation/task")
			.headers(XUIHeaders.headers_tm10)
			.body(StringBody("""{"searchRequest":{"search_parameters":[{"key":"location","operator":"IN","values":["231596","698118","198444","386417","512401","227101","562808","765324"]},{"key":"user","operator":"IN","values":[]}],"sorting_parameters":[{"sort_by":"dueDate","sort_order":"asc"}]},"view":"TaskManager"}""")))

    .pause(Environment.constantthinkTime)

  val assignTaskForCompletion =

    feed(feedCompleteTaskListFeeder)

    .exec(http("XUI_OpenTask")
			.get("/workallocation/task/${taskId}")
			.headers(XUIHeaders.headers_tm0))

    // .exec(http("request_11")
		// 	.get("/workallocation/location")
		// 	.headers(XUIHeaders.headers_tm0))

    // .exec(http("request_13")
		// 	.get("/api/monitoring-tools")
		// 	.headers(XUIHeaders.headers_tm0))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_AssignTask_005")
			.post("/workallocation/task/${taskId}/assign")
			.headers(XUIHeaders.headers_assign33)
			.body(StringBody("""{"userId":"${idamId}"}""")))

    .exec(http("XUI_AssignTask_010")
			.get("/api/healthCheck?path=%2Ftasks%2Ftask-manager%23manage_${taskId}")
			.headers(XUIHeaders.headers_assign35))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_OpenTaskManager_005")
			.get("/workallocation/location")
			.headers(XUIHeaders.headers_assign35))

    .exec(http("XUI_OpenTaskManager_020")
			.post("/workallocation/task")
			.headers(XUIHeaders.headers_tm10)
			.body(StringBody("""{"searchRequest":{"search_parameters":[{"key":"location","operator":"IN","values":["231596","698118","198444","386417","512401","227101","562808","765324"]},{"key":"user","operator":"IN","values":[]}],"sorting_parameters":[{"sort_by":"dueDate","sort_order":"asc"}]},"view":"TaskManager"}""")))

    .pause(Environment.constantthinkTime)

  val completeTask =

    exec(http("XUI_OpenTask")
			.get("/workallocation/task/${taskId}")
			.headers(XUIHeaders.headers_tm0))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_CompleteTask")
			.post("/workallocation/task/${taskId}/complete")
			.headers(XUIHeaders.headers_complete))

    .exec(http("XUI_OpenTaskManager_005")
			.get("/workallocation/location")
			.headers(XUIHeaders.headers_assign35))

    .exec(http("XUI_OpenTaskManager_020")
			.post("/workallocation/task")
			.headers(XUIHeaders.headers_tm10)
			.body(StringBody("""{"searchRequest":{"search_parameters":[{"key":"location","operator":"IN","values":["231596","698118","198444","386417","512401","227101","562808","765324"]},{"key":"user","operator":"IN","values":[]}],"sorting_parameters":[{"sort_by":"dueDate","sort_order":"asc"}]},"view":"TaskManager"}""")))

  val cancelTask =

    feed(taskCancelListFeeder)

    .exec(http("XUI_OpenTask")
			.get("/workallocation/task/${taskId}")
			.headers(XUIHeaders.openTaskHeader))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_CancelTask_005")
			.post("/workallocation/task/${taskId}/cancel")
			.headers(XUIHeaders.headers_ct24)
			.body(StringBody("{}")))

    .exec(http("XUI_CancelTask_010")
			.get("/api/healthCheck?path=%2Ftasks%2Ftask-manager%23manage_${taskId}")
			.headers(XUIHeaders.headers_ct25))

    .exec(http("XUI_CancelTask_015")
			.get("/workallocation/location")
			.headers(XUIHeaders.headers_ct26))
    
    .exec(http("XUI_OpenTaskManager_020")
			.post("/workallocation/task")
			.headers(XUIHeaders.headers_ct31)
			.body(StringBody("""{"searchRequest":{"search_parameters":[{"key":"location","operator":"IN","values":["231596","698118","198444","386417","512401","227101","562808","765324"]},{"key":"user","operator":"IN","values":[]}],"sorting_parameters":[{"sort_by":"dueDate","sort_order":"asc"}]},"view":"TaskManager"}""")))

    .pause(Environment.constantthinkTime)

  val openTaskList =

    exec(http("XUI_OpenTaskList_005")
      .get("/auth/isAuthenticated")
			.headers(XUIHeaders.headers_tl1))

    .exec(http("XUI_OpenTaskList_010")
			.get("/api/healthCheck?path=%2Ftasks%2Flist")
			.headers(XUIHeaders.headers_tl1))

    .exec(http("XUI_OpenTaskList_015")
			.get("/api/healthCheck?path=%2Ftasks%2Flist")
			.headers(XUIHeaders.headers_tl1))

    .exec(http("XUI_OpenTaskManager_020")
			.post("/workallocation/task")
			.headers(XUIHeaders.headers_tl5)
			.body(StringBody("""{"searchRequest":{"search_parameters":[{"key":"user","operator":"IN","values":["${idamId}"]}],"sorting_parameters":[{"sort_by":"dueDate","sort_order":"asc"}]},"view":"MyTasks"}"""))
      .check(regex("""id":"(.*)","name""").saveAs("taskId"))
      // .check(regex("""case_id":"(.*)","case_category""").saveAs("caseId"))
      )

    .pause(Environment.constantthinkTime)

  val OpenTask =

    // feed(feedCompleteTaskListFeeder)

    exec(http("XUI_OpenTask_005")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.headers_tl1))

    .exec(http("XUI_OpenTask_010")
			.get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}")
			.headers(XUIHeaders.headers_apihealthcheck))

    .exec(http("XUI_OpenTask_015")
			.get("/data/internal/cases/${caseId}")
			.headers(XUIHeaders.headers_viewcase))

  val EndAppealCaseEvent =

    exec(http("XUI_EndAppealSelect_005")
			.get(baseURL + "/data/internal/cases/${caseId}/event-triggers/endAppeal?ignore-warning=false")
			.headers(XUIHeaders.headers_starteventtrigger)
      .check(jsonPath("$.event_token").saveAs("eventToken")))

    .exec(http("XUI_EndAppealSelect_010")
			.get(baseURL + "/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FendAppeal%2FendAppealendAppeal")
			.headers(XUIHeaders.headers_apihealthcheck))

    .exec(http("XUI_EndAppealSelect_015")
			.get(baseURL + "/data/internal/profile")
			.headers(XUIHeaders.headers_userprofile))

    .pause(Environment.constantthinkTime)
    
    .exec(http("XUI_EndAppealPage1_005")
			.post(baseURL + "/data/case-types/Asylum/validate?pageId=endAppealendAppeal")
			.headers(XUIHeaders.headers_casevalidate)
			.body(ElFileBody("XUICaseEvent_EndAppeal1.json")))

    .exec(http("XUI_EndAppealPage1_010")
			.get(baseURL + "/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FendAppeal%2FendAppealendAppeal2")
			.headers(XUIHeaders.headers_apihealthcheck))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_EndAppealPage2_005")
			.post(baseURL + "/data/case-types/Asylum/validate?pageId=endAppealendAppeal2")
			.headers(XUIHeaders.headers_casevalidate)
			.body(ElFileBody("XUICaseEvent_EndAppeal2.json")))

    .exec(http("XUI_EndAppealPage2_010")
			.get(baseURL + "/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FendAppeal%2Fsubmit")
			.headers(XUIHeaders.headers_apihealthcheck))

    .exec(http("XUI_EndAppealPage2_015")
			.get(baseURL + "/data/internal/profile")
			.headers(XUIHeaders.headers_userprofile))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_EndAppealSubmit_005")
			.post(baseURL + "/data/cases/${caseId}/events")
			.headers(XUIHeaders.headers_createevent)
			.body(ElFileBody("XUICaseEvent_EndAppeal3.json")))

    .exec(http("XUI_EndAppealSubmit_010")
			.get(baseURL + "/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FendAppeal%2Fconfirm")
			.headers(XUIHeaders.headers_apihealthcheck))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_ViewCase")
			.get(baseURL + "/data/internal/cases/${caseId}")
			.headers(XUIHeaders.headers_viewcase))

  val XUILogout = 

    exec(http("XUI_Logout")
        .get(baseURL + "/auth/logout")
        .headers(XUIHeaders.headers_logout))

}