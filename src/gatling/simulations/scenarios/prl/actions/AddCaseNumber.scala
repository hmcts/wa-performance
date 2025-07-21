package scenarios.prl.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui.Headers

object AddCaseNumber {
  
  val execute =

    group("XUI_PRL_AddCaseNumber_Start") {
      exec(http("XUI_PRL_AddCaseNumber_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/fl401AddCaseNumber/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .exec(Common.isAuthenticated)
      .exec(Common.apiUserDetails)

      .exec(http("XUI_PRL_AddCaseNumber_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/fl401AddCaseNumber?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.profile)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_PRL_AddCaseNumber_Page1") {
      exec(http("XUI_PRL_AddCaseNumber_Page1")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401AddCaseNumber1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/PRLAddCaseNumberPage1.json")))

      .exec(http("XUI_PRL_AddCaseNumber_Page1GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_PRL_AddCaseNumber_Submit") {
      exec(http("XUI_PRL_AddCaseNumber_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/PRLAddCaseNumberSubmit.json")))

      .exec(http("XUI_PRL_AddCaseNumber_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}"))

      .exec(Common.waJurisdictions)
      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

}
