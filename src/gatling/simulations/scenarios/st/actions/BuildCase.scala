package scenarios.st.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.common.wa._
import utils.{Common, Environment}
import xui.Headers

object BuildCase {

  val execute = {

//    doWhile(session => "taskId" != "vetNewCaseDocuments" && session("counter").as[Int] < 20, "counter") {
//
//      exec(http("XUI_SelectCaseTask")
//        .get("/workallocation/case/task/#{caseId}")
//        .headers(Headers.commonHeader)
//        .header("Accept", "application/json, text/plain, */*")
//        .header("x-xsrf-token", "#{XSRFToken}")
//        .check(jsonPath("$[?(@.type=='#{taskName}')].id").optional.saveAs("taskId"))
//        .check(jsonPath("$[?(@.type=='#{taskName}')].type").optional.saveAs("taskType"))
//      )
//
//      .pause(10)
//
//      .doIf(session => !session.contains("taskId") && session("counter").as[Int] == 20) {
//        exec(session => {
//          println("Could not retrieve task after 5 attempts")
//          session
//        })
//      }
//    }
//
//    .exec(AssignTask.execute)

    group("XUI_ST_BuildCase_Page1") {
      exec(http("XUI_ST_BuildCase_Page1_GetTask")
        .get("/cases/case-details/#{caseId}/trigger/caseworker-case-built?tid=#{taskId}")
        .headers(Headers.commonHeader))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.configUI)
      .exec(Common.TsAndCs)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_ST_BuildCase_Page1_GetCaseTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/caseworker-case-built/caseType/CriminalInjuriesCompensation/jurisdiction/ST_CIC")
        .headers(Headers.commonHeader))

      .exec(http("XUI_ST_BuildCase_Page1_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/caseworker-case-built?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.isAuthenticated)
      .exec(Common.profile)

      .exec(http("XUI_ST_BuildCase_Page1_Validate")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=caseworker-case-builtcaseBuilt")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/BuildCasePage1.json")))

      .exec(http("XUI_ST_BuildCase_Page1_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_BuildCase_Submit") {
      exec(http("XUI_ST_BuildCase_Submit_CaseEvent")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/BuildCasePage2.json")))

      .exec(http("XUI_ST_BuildCase_Submit_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"actionByEvent":true,"eventName":"Case: Build case"}""")))

      .exec(http("XUI_ST_BuildCase_Submit_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.waJurisdictions)
      .exec(Common.manageLabellingRoleAssignment)
    }
  }
}