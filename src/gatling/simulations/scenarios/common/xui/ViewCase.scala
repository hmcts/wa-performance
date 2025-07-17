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
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
    )

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_IAC_SelectCaseTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(Headers.commonHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .check(jsonPath("$[0].id").optional.saveAs("taskId"))
      .check(jsonPath("$[0].type").optional.saveAs("taskType")))

    //Save taskType from response
    .exec(session => {
      // Initialise task type in session if it's not already present, ensure the variable exists before entering Loop
      session("taskType").asOption[String] match {
        case Some(taskType) => session
        case None => session.set("taskType", "")
      }
    })

    // Loop until the task type matches "reviewTheAppeal"
    .asLongAs(session => session("taskType").as[String] != "reviewTheAppeal") {
      exec(http("XUI_IAC_SelectCaseTaskRepeat")
        .get("/workallocation/case/task/#{caseId}")
        .headers(Headers.commonHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$[0].id").optional.saveAs("taskId"))
        .check(jsonPath("$[0].type").optional.saveAs("taskType")))

        .pause(5, 10) // Wait between retries

      //   // Log task Type
      //   .exec (session => {
      //     println(s"Current Task Type: ${session("taskType").as[String]}")
      //     session
      // })
    }

    .pause(Environment.constantthinkTime)

}