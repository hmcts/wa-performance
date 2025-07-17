package scenarios.iac.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.DateUtils
import utils.{Common, Environment}
import xui.Headers

object RequestRespondentEvidence {

  val execute = {

    doIf("#{todayDate.isUndefined()}") {
      exec(_.set("todayDate", DateUtils.getDateNow("yyyy-MM-dd")))
    }

      .group("XUI_IAC_RequestRespondentEvidence_EventTrigger") {
        exec(http("XUI_IAC_RequestRespondentEvidence_010_GetCaseTasks")
          .get("/case/IA/Asylum/#{caseId}/trigger/requestRespondentEvidence?tid=#{taskId}")
          .headers(Headers.commonHeader))

          .exec(http("XUI_IAC_RequestRespondentEvidence_010_EventTrigger")
            .get("/case/IA/Asylum/#{caseId}/trigger/requestRespondentEvidence")
            .headers(Headers.commonHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"))

          .exec(Common.configurationui)
          .exec(Common.configJson)
          .exec(Common.TsAndCs)
          .exec(Common.configUI)
          .exec(Common.apiUserDetails)
          .exec(Common.monitoringTools)
          .exec(Common.isAuthenticated)

          .exec(http("XUI_IAC_RequestRespondentEvidence_010_GetCase")
            .get("/data/internal/cases/#{caseId}")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
            .header("content-type", "application/json"))

          .exec(Common.profile)

          .exec(http("XUI_IAC_RequestRespondentEvidence_010_EventTrigger")
            .get("/data/internal/cases/#{caseId}/event-triggers/requestRespondentEvidence?ignore-warning=false")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
            .header("content-type", "application/json")
            .check(jsonPath("$.event_token").saveAs("eventToken")))

          .exec(Common.isAuthenticated)
      }

      .pause(Environment.constantthinkTime)

      .group("XUI_IAC_RequestRespondentEvidence_Validate") {
        exec(http("XUI_IAC_RequestRespondentEvidence_020_Validate")
          .post("/data/case-types/Asylum/validate?pageId=requestRespondentEvidencerequestRespondentEvidence")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{xsrfToken}")
          .body(ElFileBody("xuiBodies/XUIrequestRespondentEvidence1.json")))

          .exec(Common.apiUserDetails)
      }

      .pause(Environment.constantthinkTime)

      .group("XUI_IAC_RequestRespondentEvidence_Submit") {
        exec(http("XUI_IAC_RequestRespondentEvidence_030_SubmitEvent")
          .post("/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{xsrfToken}")
          .body(ElFileBody("xuiBodies/XUIrequestRespondentEvidence2.json")))

          .exec(Common.apiUserDetails)

          .exec(http("XUI_IAC_RequestRespondentEvidence_030_CompleteTask")
            .post("/workallocation/task/#{taskId}/complete")
            .headers(Headers.commonHeader)
            .header("content-type", "application/json")
            .header("x-xsrf-token", "#{xsrfToken}")
            .body(StringBody("{}")))

          .exec(http("XUI_IAC_RequestRespondentEvidence_030_ViewCase")
            .get("/data/internal/cases/#{caseId}")
            .headers(Headers.commonHeader)
            .header("content-type", "application/json")
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

          .exec(Common.apiUserDetails)
      }
  }

} 