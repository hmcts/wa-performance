package scenarios.et.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui.Headers

object PreAcceptance {
  
  val execute = {

    group("XUI_ET_PreAcceptance") {
      exec(http("XUI_ET_PreAcceptance_EventTrigger")
        .get("/cases/case-details/#{caseId}/trigger/preAcceptanceCase/preAcceptanceCase1")
        .headers(Headers.commonHeader))

      .exec(Common.configurationui)
      .exec(Common.TsAndCs)
      .exec(Common.configJson)
      .exec(Common.userDetails)
      .exec(Common.configUI)
      .exec(Common.isAuthenticated)

      .exec(http("XUI_ET_PreAcceptance_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/preAcceptanceCase/caseType/ET_EnglandWales/jurisdiction/EMPLOYMENT")
        .headers(Headers.commonHeader)
        .header("accept", "application/json"))

      .exec(http("XUI_ET_PreAcceptance_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))
  }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_PreAcceptancePage1") {
      exec(http("XUI_ET_PreAcceptancePage1")
        .get("/data/internal/cases/#{caseId}/event-triggers/preAcceptanceCase?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.userDetails)
      .exec(Common.profile)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_PreAcceptancePage2") {
      exec(http("XUI_ET_PreAcceptancePage2")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=preAcceptanceCase1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("etBodies/ETPreAcceptance_Page1.json")))

      .exec(http("XUI_ET_PreAcceptancePage2_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_SubmitAcceptance") {
      exec(http("XUI_ET_SubmitAcceptance_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_ET_SubmitAcceptance_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("etBodies/ETPreAcceptance_Submit.json")))

      .exec(http("XUI_ET_SubmitAcceptance_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.waSupportedJurisdictions)
      .exec(Common.userDetails)
    }
  }
}