package scenarios.st.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.DateUtils
import utils.{Common, Environment}
import xui.Headers

object EditCase {

  val execute = {

    exec(_.setAll("todayDate" -> DateUtils.getDateNow("yyyy-MM-dd")))

    .group("XUI_ST_EditCase_Page1") {
      exec(http("XUI_ST_EditCase_Page1_GetCaseTasks")
        .get("/cases/case-details/#{caseId}/trigger/edit-case?tid=#{taskId}")
        .headers(Headers.commonHeader))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.configUI)
      .exec(Common.TsAndCs)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_ST_EditCase_Page1_GetTask")
        .get("/workallocation/case/tasks/#{caseId}/event/edit-case/caseType/CriminalInjuriesCompensation/jurisdiction/ST_CIC")
        .headers(Headers.commonHeader))

      .exec(http("XUI_ST_EditCase_Page1_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_ST_EditCase_Page1_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/edit-case?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.isAuthenticated)
      .exec(Common.profile)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_EditCase_Page2") {
      exec(http("XUI_ST_EditCase_Page2")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-casecaseCategorisationDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/EditCasePage1.json")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_EditCase_Page3") {
      exec(http("XUI_ST_EditCase_Page3")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-casedateObjects")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/EditCasePage2.json")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_EditCase_Page4") {
      exec(http("XUI_ST_EditCase_Page4")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-caseobjectSubjects")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/EditCasePage3.json")))
    }

    .pause(Environment.constantthinkTime)

    .exec(Common.postcodeLookup)

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_EditCase_Page5"){
      exec(http("XUI_ST_EditCase_Page5")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-casesubjectDetailsObject")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/EditCasePage4.json")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_EditCase_Page6"){
      exec(http("XUI_ST_EditCase_Page6")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-caseobjectContacts")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/EditCasePage5.json")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_EditCase_Page7"){
      exec(http("XUI_ST_EditCase_Page7")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-casefurtherDetailsObject")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/EditCasePage6.json")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_EditCase_Submit"){
      exec(http("XUI_ST_EditCase_Submit_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json"))

      .exec(http("XUI_ST_EditCase_Submit_CaseEvent")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("stBodies/EditCasePage7.json")))

      .exec(http("XUI_ST_EditCase_Submit_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_ST_EditCase_Submit_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.manageLabellingRoleAssignment)
      .exec(Common.waJurisdictions)
    }
    .pause(Environment.constantthinkTime)
  }
}