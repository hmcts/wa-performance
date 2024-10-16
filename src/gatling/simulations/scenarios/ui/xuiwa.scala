package scenarios

import java.text.SimpleDateFormat
import java.util.Date
import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import java.io.{BufferedWriter, FileWriter}
import java.nio.file.{Files, Paths}


object xuiwa {

  val baseURL = Environment.xuiBaseURL
  val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
  val feedIACUserData = csv("IACUserData.csv").circular
  val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
  val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular

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

  val manageCasesLogin = 

    group("XUI_Login"){
      exec(http("XUI_010_005_Login")
        .post(Environment.idamURL + "/login?client_id=xuiwebapp&redirect_uri=" + baseURL + "/oauth2/callback&state=#{state}&nonce=#{nonce}&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user%20search-user&prompt=")
        .headers(XUIHeaders.headers_login_submit)
        .formParam("username", "#{email}")
        .formParam("password", "#{password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "false")
        .formParam("_csrf", "#{csrfToken}")
        .check(status.in(200, 304, 302))).exitHereIfFailed

      .exec(http("XUI_010_010_Login")
        .get("/external/configuration-ui/")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_010_015_Login")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_010_020_Login")
        .get("/assets/config/config.json")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_010_025_Login")
        .get("/external/config/ui")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_010_030_Login")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_010_035_Login")
        .get("/api/healthCheck?path=%2Fcases")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_010_040_Login")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
      
      .exec(http("XUI_010_045_Login")
        .get("/api/monitoring-tools")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_010_050_Login")
        .get("/api/organisation")
        .headers(XUIHeaders.xuiMainHeader)
        .check(status.is(403)))

      .exec(http("XUI_010_055_Login")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader)) //2

      .exec(http("XUI_010_060_Login")
        .get("/api/healthCheck?path=%2Fwork%2Fmy-work%2Flist")
        .headers(XUIHeaders.xuiMainHeader)) //22

      .exec(http("XUI_010_070_Login")
        .get("/workallocation2/caseworker")
        .headers(XUIHeaders.xuiMainHeader)) //26

      .exec(http("XUI_010_075_Login")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader)) //27

      .exec(http("XUI_010_080_Login")
        .get("/workallocation2/location")
        .headers(XUIHeaders.xuiMainHeader)) //30

      .exec(http("XUI_010_085_Login")
        .get("/workallocation2/task/types-of-work")
        .headers(XUIHeaders.xuiMainHeader)) //31

      .exec(http("XUI_010_090_Login")
        .get("/data/internal/case-types/Asylum/work-basket-inputs")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-workbasket-input-details.v2+json;charset=UTF-8")
        .header("content-type", "application/json")) //32

      // .exec(http("XUI_010_095_Login")
      //   .post("/workallocation/task")
      //   .headers(XUIHeaders.xuiMainHeader) //33
      //   .body(ElFileBody("xuiBodies/MyWork.json"))
      //   .header("content-type", "application/json"))
    }
    .exitHereIfFailed

    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain("manage-case.perftest.platform.hmcts.net").saveAs("xsrfToken")))
    .exec(getCookieValue(CookieKey("__auth__").saveAs("bearerToken")))

    .pause(Environment.constantthinkTime)

  val manageCasesLoginSenior = 

    feed(feedWASeniorUserData)

    .group("XUI_Login") {
      exec(http("XUI_020_005_SignIn")
        .post(Environment.idamURL + "/login?client_id=xuiwebapp&redirect_uri=" + baseURL + "/oauth2/callback&state=#{state}&nonce=#{nonce}&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user&prompt=")
        .formParam("username", "#{email}")
        .formParam("password", "#{password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "false")
        .formParam("_csrf", "#{csrfToken}")
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
        exec(http("XUI_020_020_AcceptT&CAccessJurisdictions#{count}")
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

    .exec(http("XUI_OpenTaskManager_015")
			.get("/workallocation/location")
			.headers(XUIHeaders.headers_tm0))

    .exec(http("XUI_OpenTaskManager_020")
			.post("/workallocation/task")
			.headers(XUIHeaders.headers_tm10)
      .body(StringBody("""{"searchRequest":{"search_parameters":[{"key":"location","operator":"IN","values":["231596","698118","198444","386417","512401","227101","562808","765324"]},{"key":"user","operator":"IN","values":[]}],"sorting_parameters":[{"sort_by":"dueDate","sort_order":"asc"}]},"view":"TaskManager"}""")))

    .pause(Environment.constantthinkTime)

  val assignTask =

    exec(http("XUI_OpenTask")
			.get("/workallocation/task/#{taskId}")
			.headers(XUIHeaders.headers_tm0))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_AssignTask_005")
			.post("/workallocation/task/#{taskId}/assign")
			.headers(XUIHeaders.headers_assign33)
			.body(StringBody("""{"userId":"#{idamId}"}""")))

    .exec(http("XUI_AssignTask_010")
			.get("/api/healthCheck?path=%2Ftasks%2Ftask-manager%23manage_#{taskId}")
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
			.get("/workallocation/task/#{taskId}")
			.headers(XUIHeaders.headers_tm0))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_CompleteTask")
			.post("/workallocation/task/#{taskId}/complete")
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

    .group("XUI_OpenTask"){
      exec(http("XUI_OpenTask_005_GetUserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_OpenTask_010_GetRoles")
        .get("/workallocation/task/#{taskId}/roles")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_OpenTask_015")
        .get("/workallocation/task/#{taskId}")
        .headers(XUIHeaders.xuiMainHeader))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_CancelTask") {
      exec(http("XUI_CancelTask_005_Cancel")
        .post("/workallocation/task/#{taskId}/cancel")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"hasNoAssigneeOnComplete":false}""")))

      .exec(http("XUI_CancelTask_015_GetJurisdictions")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_CancelTask_020_GetUserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_CancelTask_025_AllWork")
        .post("/workallocation/task")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/AllWork.json")))
    }

    .pause(Environment.constantthinkTime)

  val openTaskList =

    group("XUI_OpenTask") {
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
        .body(StringBody("""{"searchRequest":{"search_parameters":[{"key":"user","operator":"IN","values":["#{idamId}"]}],"sorting_parameters":[{"sort_by":"dueDate","sort_order":"asc"}]},"view":"MyTasks"}"""))
        .check(regex("""id":"(.*)","name""").saveAs("taskId"))
        // .check(regex("""case_id":"(.*)","case_category""").saveAs("caseId"))
        )
    }

    .pause(Environment.constantthinkTime)

  val OpenTask =

    group("XUI_OpenTask") {
      exec(http("XUI_OpenTask_005")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.headers_tl1))

      .exec(http("XUI_OpenTask_010")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F#{caseId}")
        .headers(XUIHeaders.headers_apihealthcheck))

      .exec(http("XUI_OpenTask_015")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.headers_viewcase))
    }

  val AssignRoles = 

    group("XUI_AssignRoles_ViewRolesTab") {
      exec(http("XUI_AssignRoles_ViewRolesTab_005")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_AssignRoles_ViewRolesTab_010")
        .post("/api/role-access/roles/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))

      .exec(http("XUI_AssignRoles_ViewRolesTab_015")
        .post("/api/role-access/exclusions/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_AllocateLegalOpsRole") {
      exec(http("XUI_AllocateLegalOpsRole_005")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_AllocateLegalOpsRole_010")
        .post("/api/role-access/allocate-role/valid-roles")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"serviceIds":["IA"]}""")))

      .exec(http("XUI_AllocateLegalOpsRole_015")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
    }

		.pause(Environment.constantthinkTime)

    .exec(_.set("currentDate", Common.getDate()))
    
    .group("XUI_ConfirmRoleAllocation") {
      exec(http("XUI_ConfirmRoleAllocation_005")
        .post("/api/role-access/allocate-role/confirm")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/XUIallocateRolesConfirm.json")))

      .exec(http("XUI_ConfirmRoleAllocation_010")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ConfirmRoleAllocation_015")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ConfirmRoleAllocation_025")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ConfirmRoleAllocation_030")
        .post("/api/role-access/exclusions/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))

      .exec(http("XUI_ConfirmRoleAllocation_035")
        .post("/api/role-access/roles/post")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))
    }

    .pause(Environment.constantthinkTime)

  val ViewTasksTab = 

    group("XUI_ViewTasksTab") {
      exec(http("XUI_ViewTasksTab")
        .get("/workallocation/case/task/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .check(jsonPath("$[0].id").saveAs("taskId")))

      .exec(http("XUI_ViewTasksTab_ApiUserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
    }

    .pause(Environment.constantthinkTime)

  val AssignTask = 

    group("XUI_AssignTaskToMe") {
      exec(http("XUI_AssignTaskToMe_Claim")
        .post("/workallocation/task/#{taskId}/claim")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{}""")))

      .exec(http("XUI_AssignTaskToMe_GetTask")
        .get("/workallocation/case/task/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader))
    }

    .pause(Environment.constantthinkTime)

  val XUILogout = 

    exec(http("XUI_Logout")
        .get(baseURL + "/auth/logout")
        .headers(XUIHeaders.headers_logout))

  val RequestRespondentEvidence =

    group("XUI_RequestRespondentEvidence_EventTrigger") {
      exec(http("XUI_RequestRespondentEvidence_010_Request")
        .get("/case/IA/Asylum/#{caseId}/trigger/requestRespondentEvidence")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"))

      .exec(http("XUI_RequestRespondentEvidence_010_ConfigurationUI")
        .get("/external/configuration-ui/")
        .headers(XUIHeaders.xuiMainHeader) //49
        .header("accept", "*/*"))
      
      .exec(http("XUI_RequestRespondentEvidence_010_ConfigJson")
        .get("/assets/config/config.json")
        .headers(XUIHeaders.xuiMainHeader)) //50

      .exec(http("XUI_RequestRespondentEvidence_010_T&C")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.xuiMainHeader)) //50

      .exec(http("XUI_RequestRespondentEvidence_010_ConfigUI")
        .get("/external/config/ui")
        .headers(XUIHeaders.xuiMainHeader)) //50

      .exec(http("XUI_RequestRespondentEvidence_010_UserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader)) //50

      .exec(http("XUI_RequestRespondentEvidence_010_MonitoringTools")
        .get("/api/monitoring-tools")
        .headers(XUIHeaders.xuiMainHeader)) //50

      .exec(http("XUI_RequestRespondentEvidence_010_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader)) //50

      .exec(http("XUI_RequestRespondentEvidence_010_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("content-type", "application/json")) //65

      .exec(http("XUI_RequestRespondentEvidence_010_Profile")
        .get("/data/internal/profile")
        .headers(XUIHeaders.xuiMainHeader) //66
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8")
        .header("content-type", "application/json"))

      .exec(http("XUI_RequestRespondentEvidence_010_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/requestRespondentEvidence?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader) //67
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(http("XUI_RequestRespondentEvidence_010_IsAuthenticated")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader)) //68
    }

    .pause(Environment.constantthinkTime)
            
    .group("XUI_RequestRespondentEvidence_Validate") {
      exec(http("XUI_RequestRespondentEvidence_020_Validate")
        .post("/data/case-types/Asylum/validate?pageId=requestRespondentEvidencerequestRespondentEvidence")
        .headers(XUIHeaders.xuiMainHeader) //78
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/XUIrequestRespondentEvidence1.json")))

      .exec(http("XUI_RequestRespondentEvidence_020_UserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader)) //80
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_RequestRespondentEvidence_Submit") {
      exec(http("XUI_RequestRespondentEvidence_030_SubmitEvent")
        .post("/data/cases/#{caseId}/events")
        .headers(XUIHeaders.xuiMainHeader) //86
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/XUIrequestRespondentEvidence2.json")))
      
      .exec(http("XUI_RequestRespondentEvidence_030_UserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader)) //89

      /*.exec(http("XUI_RequestRespondentEvidence_030_WorkAllocationSearch")
        .post("/workallocation/searchForCompletable")
        .headers(XUIHeaders.xuiMainHeader) //90
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/XUIsearchForCompletable.json")))*/

      .exec(http("XUI_RequestRespondentEvidence_030_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(XUIHeaders.xuiMainHeader) //92
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))
    }

    .group("XUI_RequestRespondentEvidence_ViewCase") {
      exec(http("XUI_RequestRespondentEvidence_040_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader) //94
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))
 
      .exec(http("XUI_RequestRespondentEvidence_040_UserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader)) //97

      .exec(http("XUI_RequestRespondentEvidence_040_UserDetails")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader)) //121
    }   
/*
    // Define the path to the CSV file
    val csvPath = Paths.get("src/gatling/resources/IACCaseData.csv")

    // Read the CSV file into a list of rows
    val lines = Files.readAllLines(csvPath).toArray(Array[String]())

    // Remove the desired row
    val rowToRemove = 1 // Index of the row to remove (0-based)
    val updatedLines = lines.zipWithIndex.filterNot { case (_, index) => index == rowToRemove }.map(_._1)

    // Write the updated list of rows back to the CSV file
    Files.write(csvPath, updatedLines.mkString("\n").getBytes)

 */
}