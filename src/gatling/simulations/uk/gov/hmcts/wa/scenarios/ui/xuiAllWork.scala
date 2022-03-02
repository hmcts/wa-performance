package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._
import java.io.{BufferedWriter, FileWriter}

object xuiAllWork {

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
  val patternDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val now = LocalDate.now()
  // now.plusDays(7).format(datePattern)

  /*val AssignTask = 

    exec(http("request_0")
			.get("/api/healthCheck?path=%2Fwork%2Fall-work%2Ftasks")
			.headers(headers_0))

    .exec(http("request_1")
			.get("/api/user/details")
			.headers(headers_1))

    .exec(http("request_2")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(headers_2))
            
    .exec(http("request_3")
			.post("/workallocation2/task")
			.headers(headers_3)
			.body(RawFileBody("allWorkassignTask1_0003_request.txt")))

		.pause(4)

		.exec(http("request_4")
			.get("/api/user/details")
			.headers(headers_4))

		.pause(4)

		.exec(http("request_6")
			.get("/workallocation2/task/b1226682-7204-11ec-b835-528fa55456e5/roles")
			.headers(headers_6))

    .exec(http("request_8")
			.get("/workallocation2/task/b1226682-7204-11ec-b835-528fa55456e5")
			.headers(headers_8))

    .exec(http("request_9")
			.get("/api/user/details")
			.headers(headers_9))

		.pause(1)

		.exec(http("request_10")
			.get("/api/user/details")
			.headers(headers_10))

		.pause(9)

		.exec(http("request_11")
			.get("/api/user/details")
			.headers(headers_11))

		.pause(2)

		.exec(http("request_14")
			.post("/workallocation2/task/b1226682-7204-11ec-b835-528fa55456e5/assign")
			.headers(headers_14)
			.body(StringBody("""{"userId":"082654c0-f651-4cd1-9129-d8dd02de50ed"}""")))

    .exec(http("request_15")
			.get("/api/healthCheck?path=%2Fwork%2Fall-work%2Ftasks")
			.headers(headers_15))

    .exec(http("request_16")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(headers_16))

    .exec(http("request_17")
			.get("/api/user/details")
			.headers(headers_17))

    .exec(http("request_18")
			.post("/workallocation2/task")
			.headers(headers_18)
			.body(ElFileBody("/resources/xuiBodies/XUISearchRequest.json")))

  */

  val allWorkTasks = 

    group("XUI_001_ViewAllWork") {
      exec(http("XUI_001_ViewAllWork_005")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))
      
      .exec(http("XUI_001_ViewAllWork_010")
        .get("/api/healthCheck?path=%2Fwork%2Fall-work%2Ftasks")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_001_ViewAllWork_015")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_001_ViewAllWork_020")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_001_ViewAllWork_025")
        .post("/workallocation2/task")
        .headers(XUIHeaders.xuiMainHeader) //10
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(ElFileBody("xuiBodies/AllWork.json"))
        // .check(jsonPath("$.tasks[0].id").saveAs("taskId"))
        // .check(jsonPath("$.tasks[0].case_id").saveAs("caseId"))
        )
    }

		.pause(Environment.constantthinkTime)

  val allWorkViewTask = 

    group("XUI_002_ViewTask") {
      exec(http("XUI_002_ViewTask_005")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_002_ViewTask_010")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_002_ViewTask_015")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%2Ftasks")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_002_ViewTask_020")
        .get("/data/internal/cases/${caseId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_002_ViewTask_025")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_002_ViewTask_030")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_002_ViewTask_035")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_002_ViewTask_040")
        .get("/workallocation2/case/task/${caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .check(jsonPath("$[0].id").saveAs("taskId")))
    }

    .exec(http("XUI_002_ViewTask_GetJudicialUsersNull")
      .post("/api/role-access/roles/getJudicialUsers")
      .headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
      .body(StringBody("""{"userIds":[null],"services":["IA"]}""")))

    .pause(Environment.constantthinkTime) 
    
    .group("XUI_003_AssignTask") {
      exec(http("XUI_003_AssignTask_005")
        .post("/workallocation2/task/${taskId}/claim")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{}""")))

      .exec(http("XUI_003_AssignTask_010")
        .get("/workallocation2/case/task/${caseId}")
        .headers(XUIHeaders.xuiMainHeader))
    }

    .exec(http("XUI_003_AssignTask_GetJudicialUsers")
      .post("/api/role-access/roles/getJudicialUsers")
      .headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
      .body(StringBody("""{"userIds":[],"services":["IA"]}""")))

    .pause(Environment.constantthinkTime)

  val judicialUserAllWork = 

    group("XUI_Judicial_001_ViewAllWork") {
      exec(http("XUI_Judicial_001_ViewAllWork_005")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))
      
      .exec(http("XUI_Judicial_001_ViewAllWork_010")
        .get("/api/healthCheck?path=%2Fwork%2Fall-work%2Ftasks")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_001_ViewAllWork_015")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_001_ViewAllWork_020")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_001_ViewAllWork_025")
        .post("/workallocation2/task")
        .headers(XUIHeaders.xuiMainHeader) //10
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(ElFileBody("xuiBodies/AllWorkJudicial.json"))
        .check(jsonPath("$.tasks[0].id").saveAs("taskId"))
        .check(jsonPath("$.tasks[0].case_id").saveAs("caseId")))
    }

		.pause(Environment.constantthinkTime)

  val judicialUserOpenCase =

    group("XUI_Judicial_002_OpenCase") {
      exec(http("XUI_Judicial_002_OpenCase_005")
        .get("/cases/case-details/${caseId}")
        .headers(XUIHeaders.xuiMainHeader))

      // .exec {
      //   session =>
      //     println(session("caseId").as[String])
      //     session
      // }

      .exec(http("XUI_Judicial_002_OpenCase_010")
        .get("/external/configuration-ui/")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_015")
        .get("/assets/config/config.json")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_020")
        .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_025")
        .get("/external/config/ui")
        .headers(XUIHeaders.xuiMainHeader))
      
      .exec(http("XUI_Judicial_002_OpenCase_030")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_035")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_040")
        .get("/api/monitoring-tools")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_002_OpenCase_045")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%23overview")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_002_OpenCase_050")
        .get("/data/internal/cases/${caseId}")
        .headers(XUIHeaders.xuiMainHeader)) //29

      .exec(http("XUI_Judicial_002_OpenCase_055")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_002_OpenCase_060")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}")
        .headers(XUIHeaders.xuiMainHeader))
      
      .exec(http("XUI_Judicial_002_OpenCase_065")
        .get("/data/internal/cases/${caseId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_070")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_075")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%23Overview")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_002_OpenCase_080")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
    }

		.pause(Environment.constantthinkTime)

  val judicialUserAllocateRole = 
      
    group("XUI_Judicial_003_AddRole") {
      exec(http("XUI_Judicial_003_AddRole_005")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%2Froles-and-access")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_003_AddRole_010")
        .get("/data/internal/cases/${caseId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_003_AddRole_015")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_003_AddRole_020")
        .post("/api/role-access/exclusions/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"userIds":[],"services":["IA"]}""")))

      .exec(http("XUI_Judicial_003_AddRole_025")
        .post("/api/role-access/roles/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"caseId":"${caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))

      .exec(http("XUI_Judicial_003_AddRole_030")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_003_AddRole_035")
        .get("/api/healthCheck?path=%2Frole-access%2Fallocate-role%2Fallocate%3FcaseId%3D${caseId}%26jurisdiction%3DIA%26roleCategory%3DJUDICIAL")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_003_AddRole_040")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
    }

    .exec(http("XUI_Judicial_003_AddRole_GetJudicialUsers")
      .post("/api/role-access/roles/getJudicialUsers")
      .headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
      .body(StringBody("""{"userIds":[],"services":["IA"]}""")))
      
		.pause(Environment.constantthinkTime)

    .exec(_.setAll( "currentDate" -> now.format(patternDate),
                    "currentDatePlusSeven" -> now.plusDays(7).format(patternDate)))

    .group("XUI_Judicial_004_ConfirmRoleAllocation") {
      exec(http("XUI_Judicial_004_ConfirmRoleAllocation_005")
        .post("/api/role-access/allocate-role/confirm")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
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
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_015")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%2Froles-and-access")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_020")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_025")
        .post("/api/role-access/exclusions/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"caseId":"${caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))
        
      .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_030")
        .post("/api/role-access/roles/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"caseId":"${caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))
    }

    .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_GetJudicialUsersIdam")
      .post("/api/role-access/roles/getJudicialUsers")
      .headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
      .body(StringBody("""{"userIds":["${idamId}"],"services":["IA"]}""")))

    .exec(http("XUI_Judicial_004_ConfirmRoleAllocation_GetJudicialUsers")
      .post("/api/role-access/roles/getJudicialUsers")
      .headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
      .body(StringBody("""{"userIds":[],"services":["IA"]}""")))

    .pause(Environment.constantthinkTime)

  val judicialUserRemoveRole = 

    group("XUI_Judicial_005_RemoveRole") {
      exec(http("XUI_Judicial_005_RemoveRole_005")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_005_RemoveRole_010")
        .get("/api/healthCheck?path=%2Frole-access%2Fallocate-role%2Fremove%3FcaseId%3D${caseId}%26assignmentId%3D${roleAllocateId}%26caseType%3DAsylum%26jurisdiction%3DIA%26typeOfRole%3Dlead-judge%26roleCategory%3DJUDICIAL%26actorId%3D${idamId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_005_RemoveRole_015")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_005_RemoveRole_020")
        .post("/api/role-access/roles/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"caseId":"${caseId}","jurisdiction":"IA","caseType":"Asylum","assignmentId":"${roleAllocateId}"}""")))
    }
        
    .exec(http("XUI_Judicial_005_RemoveRole_GetJudicialUsersIdam")
      .post("/api/role-access/roles/getJudicialUsers")
      .headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
      .body(StringBody("""{"userIds":["${idamId}"],"services":["IA"]}""")))

    .pause(Environment.constantthinkTime)

    .group("XUI_Judicial_006_ConfirmRemoveRole") {
      exec(http("XUI_Judicial_006_ConfirmRemoveRole_005")
        .post("/api/role-access/allocate-role/delete")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"assigmentId":"${roleAllocateId}"}""")))

      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_010")
        .get("/auth/isAuthenticated")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_015")
        .get("/api/healthCheck?path=%2Fcases%2Fcase-details%2F${caseId}%2Froles-and-access")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_020")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_025")
        .post("/api/role-access/exclusions/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"caseId":"${caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))

      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_030")
        .get("/api/user/details")
        .headers(XUIHeaders.xuiMainHeader))
        
      .exec(http("XUI_Judicial_006_ConfirmRemoveRole_035")
        .post("/api/role-access/roles/post")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "${xsrfToken}")
        .body(StringBody("""{"caseId":"${caseId}","jurisdiction":"IA","caseType":"Asylum"}""")))
    }

    .exec(http("XUI_Judicial_006_ConfirmRemoveRole_GetJudicialUsersNull")
      .post("/api/role-access/roles/getJudicialUsers")
      .headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
      .body(StringBody("""{"userIds":[],"services":["IA"]}""")))

    .pause(Environment.constantthinkTime)

}