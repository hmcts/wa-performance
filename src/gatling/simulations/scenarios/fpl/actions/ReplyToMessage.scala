package scenarios.fpl.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui.Headers

object ReplyToMessage {

  val execute = {

    group("XUI_FPL_ReplyToMessage_Start") {
      exec(http("XUI_FPL_ReplyToMessage_GetTasks")
        .get("/case/PUBLICLAW/CARE_SUPERVISION_EPO/#{caseId}/trigger/replyToMessageJudgeOrLegalAdviser?tid=#{taskId}")
        .headers(Headers.commonHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.apiUserDetails)
      .exec(Common.isAuthenticated)

      .exec(http("XUI_FPL_ReplyToMessage_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.profile)

      .exec(http("XUI_FPL_ReplyToMessage_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/replyToMessageJudgeOrLegalAdviser?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(http("XUI_FPL_ReplyToMessage_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/replyToMessageJudgeOrLegalAdviser/caseType/CARE_SUPERVISION_EPO/jurisdiction/PUBLICLAW")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("content-type", "application/json"))

      .exec(http("XUI_FPL_ReplyToMessage_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/replyToMessageJudgeOrLegalAdviser/caseType/CARE_SUPERVISION_EPO/jurisdiction/PUBLICLAW")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_FPL_ReplyToMessage_Page1") {
      exec(http("XUI_FPL_ReplyToMessage_Page1")
        .post("/data/case-types/CARE_SUPERVISION_EPO/validate?pageId=replyToMessageJudgeOrLegalAdviserSelectMessage")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/FPLSendMessage1.json")))

      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_FPL_ReplyToMessage_Page2") {
      exec(http("XUI_FPL_ReplyToMessage_Page2")
        .post("/data/case-types/CARE_SUPERVISION_EPO/validate?pageId=replyToMessageJudgeOrLegalAdviserReplyToMessage")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/FPLSendMessage2.json")))

      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_FPL_ReplyToMessage_Submit") {
      exec(http("XUI_FPL_ReplyToMessage_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/FPLSendMessageComplete.json")))

      .exec(http("XUI_FPL_ReplyToMessage_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}"))

      .exec(http("XUI_FPL_ReplyToMessage_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.waJurisdictions)
      .exec(Common.apiUserDetails)
    }
  }
}