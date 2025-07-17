package scenarios.common.wa

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object AssignTask {

  val execute =

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

}