package scenarios.common.wa

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object ViewTasksTab {

  val execute =

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

}