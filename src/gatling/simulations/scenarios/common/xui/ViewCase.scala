package scenarios.common.xui

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui._

object ViewCase {

  val execute =

    exec(Common.isAuthenticated)
    .exec(Common.waSupportedJurisdictions)
    .exec(Common.apiUserDetails)

    .exec(http("XUI_ViewCase_GetCase")
      .get("/data/internal/cases/#{caseId}")
      .headers(Headers.commonHeader)
      .header("x-xsrf-token", "#{XSRFToken}")
      .header("content-type", "application/json")
      .header("experimental", "true")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
    )

    .pause(Environment.constantthinkTime)

    .exec(_.set("counter", 0))

    .doWhile(session => !session.contains("taskId") && session("counter").as[Int] < 20, "counter") {

      exec(http("XUI_SelectCaseTask")
        .get("/workallocation/case/task/#{caseId}")
        .headers(Headers.commonHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$[?(@.type=='#{taskName}')].id").optional.saveAs("taskId"))
        .check(jsonPath("$[?(@.type=='#{taskName}')].type").optional.saveAs("taskType"))
      )

      .exec { session =>
        val current = session("counter").as[Int]
        session.set("counter", current + 1)
      }

      .pause(10)

    }

    .doIf(session => !session.contains("taskId") && session("counter").as[Int] == 20){
      exec(session => {
        println("Could not retrieve task after 20 attempts")
        session
      })
    }
//
//    //Save taskType from response
//    .exec(session => {
//      // Initialise task type in session if it's not already present, ensure the variable exists before entering Loop
//      session("taskType").asOption[String] match {
//        case Some(taskType) => session
//        case None => session.set("taskType", "")
//      }
//    })
//
//    // Loop until the task type matches task set within the scenario
//    .asLongAs(session => session("taskType").as[String] != s"${session("taskName").as[String]}") {
//      exec(http("XUI_SelectCaseTaskRepeat")
//        .get("/workallocation/case/task/#{caseId}")
//        .headers(Headers.commonHeader)
//        .header("Accept", "application/json, text/plain, */*")
//        .header("x-xsrf-token", "#{XSRFToken}")
////        .check(jsonPath(s"$..[?(@.type=='${session("taskName")}')].id").optional.saveAs("taskId"))
////        .check(jsonPath("$..[?(@.type=='#{taskName}')].type").optional.saveAs("taskType"))
////        .check(jsonPath(s"$..[?(@.type=='${session("taskName")}')].type").optional.saveAs("taskType"))
////        .check(jsonPath(session => "$..[?(@.type=='"+session("taskName").as[String]+"')].id").optional.saveAs("taskId"))
////        .check(jsonPath(session => "$..[?(@.type=='"+session("taskName").as[String]+"')].type").optional.saveAs("taskType"))
//        .check(jsonPath(session => session("taskName").validate[String].map(taskName => s"""$..[?(@.type=="${taskName}")].id""")).optional.saveAs("taskId"))
//        .check(jsonPath(session => session("taskName").validate[String].map(taskName => s"""$..[?(@.type=="${taskName}")].type""")).optional.saveAs("taskType"))
//      )
//
//        .pause(5, 10) // Wait between retries
//
//         // Log task Type
//         .exec (session => {
//           println(s"Current Task Type: ${session("taskType").as[String]}")
//           session
//       })
//    }

    .pause(Environment.constantthinkTime)

}