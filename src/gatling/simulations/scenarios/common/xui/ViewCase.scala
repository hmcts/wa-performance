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
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

    .pause(Environment.constantthinkTime)

    .exec(session => session.set("counter", 0))

    .doWhile(session => !session.contains("taskId") && session("counter").as[Int] < 20, "counter") {
      group("XUI_SelectCaseTask") {
        exec(http("XUI_SelectCaseTask_#{counter}")
          .get("/workallocation/case/task/#{caseId}")
          .headers(Headers.commonHeader)
          .header("Accept", "application/json, text/plain, */*")
          .header("x-xsrf-token", "#{XSRFToken}")
          .check(jsonPath("$[?(@.type=='#{taskName}')].id").optional.saveAs("taskId"))
          .check(jsonPath("$[?(@.type=='#{taskName}')].type").optional.saveAs("taskType")))
      }

      .pause(30)
    }

//    .doIf(session => !session.contains("taskId") && session("counter").as[Int] >= 20){
//      exec(session => {
//        println("Could not retrieve task after 20 attempts")
//        session
//      })
//      .exitHere
//    }

      .exec(session => {
        if (session("counter").as[Int] >= 20) {
          println("Could not retrieve task after 20 attempts")
        }
        session
      })

      .exitHereIf(session => session("counter").as[Int] >= 20)

    .pause(Environment.constantthinkTime)

}