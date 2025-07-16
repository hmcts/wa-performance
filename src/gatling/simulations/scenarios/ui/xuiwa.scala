package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._


object xuiwa {

  val baseURL = Environment.xuiBaseURL
  val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
  val feedIACUserData = csv("IACUserData.csv").circular
  val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
  val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular

  val cancelTask =

    feed(taskCancelListFeeder)

    .group("XUI_OpenTask"){
      exec(http("XUI_OpenTask_005_GetUserDetails")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_OpenTask_010_GetRoles")
        .get("/workallocation/task/#{taskId}/roles")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_OpenTask_015")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.xuiMainHeader))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_CancelTask") {
      exec(http("XUI_CancelTask_005_Cancel")
        .post("/workallocation/task/#{taskId}/cancel")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"hasNoAssigneeOnComplete":false}""")))

      .exec(http("XUI_CancelTask_015_GetJurisdictions")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_CancelTask_020_GetUserDetails")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))

      .exec(http("XUI_CancelTask_025_AllWork")
        .post("/workallocation/task")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("xuiBodies/AllWork.json")))
    }

    .pause(Environment.constantthinkTime)

  val ViewTasksTab = 

    group("XUI_ViewTasksTab") {
      exec(http("XUI_ViewTasksTab")
        .get("/workallocation/case/task/#{caseId}")
        .headers(Headers.xuiMainHeader)
        .check(jsonPath("$[0].id").saveAs("taskId")))

      .exec(http("XUI_ViewTasksTab_ApiUserDetails")
        .get("/api/user/details")
        .headers(Headers.xuiMainHeader))
    }

    .pause(Environment.constantthinkTime)

  val AssignTask = 

    group("XUI_AssignTaskToMe") {
      exec(http("XUI_AssignTaskToMe_Claim")
        .post("/workallocation/task/#{taskId}/claim")
        .headers(Headers.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{}""")))

      .exec(http("XUI_AssignTaskToMe_GetTask")
        .get("/workallocation/case/task/#{caseId}")
        .headers(Headers.xuiMainHeader))
    }

    .pause(Environment.constantthinkTime)

  val XUILogout = 

    exec(http("XUI_Logout")
        .get(baseURL + "/auth/logout")
        .headers(Headers.headers_logout))

}