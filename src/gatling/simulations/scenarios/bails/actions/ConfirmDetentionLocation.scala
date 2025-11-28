package scenarios.bails.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui.Headers

object ConfirmDetentionLocation {

  val execute =

    group("XUI_Bails_ConfirmDetentionLocation_EventTrigger") {
      exec(http("XUI_Bails_ConfirmDetentionLocation_010_GetCaseTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/confirmDetentionLocation/caseType/Bail/jurisdiction/IA")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("processBailApplication")))

      .exec(http("XUI_Bails_ConfirmDetentionLocation_010_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/confirmDetentionLocation?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .check(jsonPath("$.event_token").saveAs("eventToken"))
        .check(substring("place of detention is correct")))

      .exec(http("XUI_Bails_ConfirmDetentionLocation_010_GetCaseTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/confirmDetentionLocation/caseType/Bail/jurisdiction/IA")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("processBailApplication")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_Bails_ConfirmDetentionLocation_Validate") {
      exec(http("XUI_Bails_ConfirmDetentionLocation_020_Validate")
        .post("/data/case-types/Bail/validate?pageId=confirmDetentionLocationconfirmDetentionLocation")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bailsBodies/ConfirmDetentionLocationValidate.json"))
        .check(substring("isDetentionLocationCorrect")))

      .exec(http("XUI_Bails_ConfirmDetentionLocation_020_ViewCase")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_Bails_ConfirmDetentionLocation_Submit") {
      exec(http("XUI_Bails_ConfirmDetentionLocation_030_SubmitEvent")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bailsBodies/ConfirmDetentionLocationSubmit.json"))
        .check(substring("applicationSubmitted"))
        .check(substring("detentionFacility")))

      .exec(http("XUI_Bails_ConfirmDetentionLocation_030_GetCaseTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/confirmDetentionLocation/caseType/Bail/jurisdiction/IA")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("processBailApplication")))

      .exec(http("XUI_Bails_ConfirmDetentionLocation_030_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_Bails_ConfirmDetentionLocation_030_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(substring("Bail application")))

      .exec(Common.waSupportedJurisdictions)
      .exec(Common.manageLabellingRoleAssignment)
    }

}
