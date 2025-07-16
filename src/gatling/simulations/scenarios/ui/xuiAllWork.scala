package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object xuiAllWork {

  val baseURL = Environment.xuiBaseURL
  val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
  val feedIACUserData = csv("IACUserData.csv").circular
  val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
  val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular
  val patternDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val now = LocalDate.now()
  
  val allWorkTasks = 

    group("XUI_001_ViewAllWork") {
      exec(http("XUI_001_ViewAllWork_005")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))
      
      .exec(http("XUI_001_ViewAllWork_010")
        .get("/api/healthCheck?path=%2Fwork%2Fall-work%2Ftasks")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_001_ViewAllWork_015")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_001_ViewAllWork_020")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_001_ViewAllWork_025")
        .post("/workallocation/task")
        .headers(Headers.xuiMainHeader) //10
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/AllWork.json"))
        // .check(jsonPath("$.tasks[0].id").saveAs("taskId"))
        // .check(jsonPath("$.tasks[0].case_id").saveAs("caseId"))
        )
    }

		.pause(Environment.constantthinkTime)

  val allWorkTasksHighPriority =

    exec(http("XUI_001_ViewAllWork_HighPriority")
      .post("/workallocation/task")
      .headers(Headers.xuiMainHeader) //10
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
      .body(ElFileBody("xuiBodies/AllWorkHighPriority.json")))

    .pause(Environment.constantthinkTime)

  val allWorkViewTask = 

    group("XUI_002_ViewTask") {
      exec(http("XUI_002_ViewTask_005")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_002_ViewTask_010")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))

      // .exec(http("XUI_002_ViewTask_015")
      //   .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F#{caseId}%2Ftasks")
      //   .headers(Headers.xuiMainHeader))

      .exec(http("XUI_002_ViewTask_020")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_002_ViewTask_040")
        .get("/workallocation2/case/task/#{caseId}")
        .headers(Headers.xuiMainHeader)
        .check(jsonPath("$[0].id").saveAs("taskId")))
    }

    // .exec(http("XUI_002_ViewTask_GetJudicialUsersNull")
    //   .post("/api/role-access/roles/getJudicialUsers")
    //   .headers(Headers.xuiMainHeader)
    //   .header("content-type", "application/json")
    //   .header("x-xsrf-token", "#{xsrfToken}")
    //   .body(StringBody("""{"userIds":[null],"services":["IA"]}""")))

    .pause(Environment.constantthinkTime) 
    
    .group("XUI_003_AssignTask") {
      exec(http("XUI_003_AssignTask_005")
        .post("/workallocation2/task/#{taskId}/claim")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{}""")))

      .exec(http("XUI_003_AssignTask_010")
        .get("/workallocation2/case/task/#{caseId}")
        .headers(Headers.xuiMainHeader))
    }

    // .exec(http("XUI_003_AssignTask_GetJudicialUsers")
    //   .post("/api/role-access/roles/getJudicialUsers")
    //   .headers(Headers.xuiMainHeader)
    //   .header("content-type", "application/json")
    //   .header("x-xsrf-token", "#{xsrfToken}")
    //   .body(StringBody("""{"userIds":[],"services":["IA"]}""")))

    .pause(Environment.constantthinkTime)

  val judicialUserAllWork = 

    group("XUI_Judicial_001_ViewAllWork") {
      exec(http("XUI_Judicial_001_ViewAllWork_005")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))
      
      .exec(http("XUI_Judicial_001_ViewAllWork_015")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_001_ViewAllWork_020")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_Judicial_001_ViewAllWork_025")
        .post("/workallocation/task")
        .headers(Headers.xuiMainHeader) //10
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/AllWorkJudicial.json"))
        .check(jsonPath("$.tasks[0].id").saveAs("taskId"))
        .check(jsonPath("$.tasks[0].case_id").saveAs("caseId")))

      .exec(http("XUI_Judicial_001_ViewAllWork_030_GetJudicialUsers")
        .post("/api/role-access/roles/getJudicialUsers")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"userIds":["#{idamId}"],"services":[]}""")))
    }

		.pause(Environment.constantthinkTime)

  val judicialUserOpenCase =

    group("XUI_Judicial_002_OpenCase") {
      exec(http("XUI_Judicial_002_OpenCase_005")
        .get("/cases/case-details/#{caseId}")
        .headers(Headers.xuiMainHeader))

      // .exec {
      //   session =>
      //     println(session("caseId").as[String])
      //     session
      // }

      .exec(http("XUI_Judicial_002_OpenCase_010")
        .get("/external/configuration-ui/")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_015")
        .get("/assets/config/config.json")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_020")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_025")
        .get("/external/config/ui")
        .headers(Headers.xuiMainHeader))
      
      .exec(http("XUI_Judicial_002_OpenCase_030")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_035")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_040")
        .get("/api/monitoring-tools")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_Judicial_002_OpenCase_050")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.xuiMainHeader)) //29
    }

		.pause(Environment.constantthinkTime)

  val judicialUserAllocateRole = 
      
    group("XUI_Judicial_003_AddRole") {
      exec(http("XUI_Judicial_003_AddRole_010")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_003_AddRole_015")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_003_AddRole_020")
        .post("/api/role-access/exclusions/post")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"userIds":[],"services":["IA"]}""")))

      .exec(http("XUI_Judicial_003_AddRole_025")
        .post("/api/role-access/roles/post")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))

      .exec(http("XUI_Judicial_003_AddRole_030")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_003_AddRole_040")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
    }

		.pause(Environment.constantthinkTime)

    .exec(_.setAll( "currentDate" -> now.format(patternDate),
                    "currentDatePlusSeven" -> now.plusDays(7).format(patternDate)))

    .group("XUI_Judicial_004_ConfirmRoleAllocation") {
      exec(http("XUI_Judicial_004_ConfirmRoleAllocation_005")
        .post("/api/role-access/allocate-role/confirm")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/AllocateJudicialRole7Days.json"))
        // .check(bodyString.saveAs("response"))
        .check(jsonPath("$.roleAssignmentResponse.requestedRoles[0].id").saveAs("roleAllocateId")))

      // .exec {
      //   session =>
      //     println(session("response").as[String])
      //     session
      // }

      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_010")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_020")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_025")
        .post("/api/role-access/exclusions/post")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))
        
      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_030")
        .post("/api/role-access/roles/post")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))

      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_035_GetJudicialUsers")
        .post("/api/role-access/roles/getJudicialUsers")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"userIds":["#{idamId}"],"services":["IA"]}"""))
        .check(status is 406))
    }

    .pause(Environment.constantthinkTime)

  val judicialUserRemoveRole = 

    group("XUI_Judicial_005_RemoveRole") {
      exec(http("XUI_Judicial_005_RemoveRole_005")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_005_RemoveRole_015")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_Judicial_005_RemoveRole_020")
        .post("/api/role-access/roles/post")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum","assignmentId":"#{roleAllocateId}"}""")))
        
      .exec(http("XUI_Judicial_005_RemoveRole_025_GetJudicialUsers")
        .post("/api/role-access/roles/getJudicialUsers")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"userIds":["#{idamId}"],"services":["IA"]}"""))
        .check(status is 406))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_Judicial_006_ConfirmRemoveRole") {
      exec(http("XUI_Judicial_006_ConfirmRemoveRole_005")
        .post("/api/role-access/allocate-role/delete")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"assigmentId":"#{roleAllocateId}"}""")))

      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_010")
        .get("/auth/isAuthenticated")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_020")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_025")
        .post("/api/role-access/exclusions/post")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))

      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_030")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
        
      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_035")
        .post("/api/role-access/roles/post")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"caseId":"#{caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))

      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_040_GetJudicialUsers")
        .post("/api/role-access/roles/getJudicialUsers")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"userIds":["#{idamId}"],"services":["IA"]}"""))
        .check(status is 406))
    }

    .pause(Environment.constantthinkTime)

}