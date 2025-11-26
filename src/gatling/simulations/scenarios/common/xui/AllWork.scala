package scenarios.common.xui

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object AllWork {

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
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("xuiBodies/AllWork.json")))
    }

		.pause(Environment.constantthinkTime)

  val allWorkTasksHighPriority =

    exec(http("XUI_001_ViewAllWork_HighPriority")
      .post("/workallocation/task")
      .headers(Headers.xuiMainHeader) //10
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("xuiBodies/AllWorkHighPriority.json")))

    .pause(Environment.constantthinkTime)

}