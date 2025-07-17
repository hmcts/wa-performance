package scenarios.common.wa

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object CancelTask {

  val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular

  val execute =

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

}