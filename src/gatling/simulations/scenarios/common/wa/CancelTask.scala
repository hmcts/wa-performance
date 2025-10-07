package scenarios.common.wa

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment
import xui._

object CancelTask {

  val execute =

    group("XUI_OpenTask"){
      exec(http("XUI_OpenTask_005_GetUserDetails")
        .get("/api/user/details")
        .headers(Headers.commonHeader))

      .exec(http("XUI_OpenTask_010_GetRoles")
        .get("/workallocation/task/#{taskId}/roles")
        .headers(Headers.commonHeader))

      .exec(http("XUI_OpenTask_015")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.commonHeader))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_CancelTask") {
      exec(http("XUI_CancelTask_005_Cancel")
        .post("/workallocation/task/#{taskId}/cancel")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"hasNoAssigneeOnComplete":false}""")))

      .exec(http("XUI_CancelTask_015_GetJurisdictions")
        .get("/api/wa-supported-jurisdiction/get")
        .headers(Headers.commonHeader))

      .exec(http("XUI_CancelTask_020_GetUserDetails")
        .get("/api/user/details")
        .headers(Headers.commonHeader))

      .exec(http("XUI_CancelTask_025_AllWork")
        .post("/workallocation/task")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("xuiBodies/AllWork.json")))
    }
}