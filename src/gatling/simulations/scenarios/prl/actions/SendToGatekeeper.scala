package scenarios.prl.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui.Headers

object SendToGatekeeper {
  
  val execute =

      group("XUI_PRL_SendToGatekeeper_Start"){
        exec(http("XUI_PRL_SendToGatekeeper_EventTrigger")
          .get("/data/internal/cases/#{caseId}/event-triggers/sendToGateKeeper?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("content-type", "application/json")
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.event_token").saveAs("eventToken")))

        .exec(http("XUI_PRL_SendToGatekeeper_GetTask")
          .get("/cases/case-details/#{caseId}/trigger/sendToGateKeeper/sendToGateKeeper1?tid=#{taskId}")
          .headers(Headers.commonHeader))

        .exec(Common.configurationui)
        .exec(Common.configJson)
        .exec(Common.configUI)
        .exec(Common.TsAndCs)
        .exec(Common.apiUserDetails)

        .exec(http("XUI_PRL_SendToGatekeeper_GetTasks")
          .get("/workallocation/case/tasks/#{caseId}/event/sendToGateKeeper/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .header("content-type", "application/json"))
      }

      .pause(Environment.constantthinkTime)

      .group("XUI_PRL_SendToGatekeeper_Page1") {
        exec(http("XUI_PRL_SendToGatekeeper_Page1")
          .post("/data/case-types/PRLAPPS/validate?pageId=sendToGateKeeper1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{XSRFToken}")
          .body(ElFileBody("xuiBodies/PRLSendToGatekeeper1.json")))

        .exec(Common.apiUserDetails)
      }

      .pause(Environment.constantthinkTime)

      .group("XUI_PRL_SendToGatekeeper_Submit") {
        exec(http("XUI_PRL_SendToGatekeeper_Submit")
          .post("/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{XSRFToken}")
          .body(ElFileBody("xuiBodies/PRLSendToGatekeeperSubmit.json")))

        .exec(http("XUI_PRL_SendToGatekeeper_CompleteTask")
          .post("/workallocation/task/#{taskId}/complete")
          .header("accept", "application/json")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{XSRFToken}"))

        .exec(Common.waJurisdictions)
        .exec(Common.apiUserDetails)
      }
}