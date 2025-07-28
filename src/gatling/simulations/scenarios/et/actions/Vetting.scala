package scenarios.et.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui.Headers

object Vetting {
  
  val execute = {

    group("XUI_ET_Vetting_Page1") {
      exec(http("XUI_ET_Vetting_GetTasks")
        .get("/cases/case-details/#{caseId}/trigger/et1Vetting/et1Vetting1?tid=#{taskId}")
        .headers(Headers.commonHeader))

      .exec(Common.configurationui)
      .exec(Common.TsAndCs)
      .exec(Common.configJson)
      .exec(Common.userDetails)
      .exec(Common.configUI)
      .exec(Common.monitoringTools)
      .exec(Common.isAuthenticated)

      .exec(http("XUI_ET_Vetting_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_ET_Vetting_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/et1Vetting?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.userDetails)
      .exec(Common.profile)
  }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page2"){
      exec(http("XUI_ET_Vetting_Page2")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page1.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page3"){
      exec(http("XUI_ET_Vetting_Page3")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page2.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page4"){
      exec(http("XUI_ET_Vetting_Page4")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page3.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page5"){
      exec(http("XUI_ET_Vetting_Page5")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page4.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page6"){
      exec(http("XUI_ET_Vetting_Page6")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting5")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page5.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page7"){
      exec(http("request_185")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting6")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page6.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page8"){
      exec(http("XUI_ET_Vetting_Page8")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting7")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page7.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page9"){
      exec(http("XUI_ET_Vetting_Page9")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting8")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page8.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page10"){
      exec(http("XUI_ET_Vetting_Page10")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting9")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page9.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page11"){
      exec(http("XUI_ET_Vetting_Page11")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting10")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page10.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page12"){
      exec(http("XUI_ET_Vetting_Page12")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting11")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page11.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page13"){
      exec(http("XUI_ET_Vetting_Page13")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting12")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page12.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Page14"){
      exec(http("XUI_ET_Vetting_Page14")
        .post("/data/case-types/ET_EnglandWales/validate?pageId=et1Vetting13")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Page13.json")))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ET_Vetting_Submit") {
      exec(http("XUI_ET_Vetting_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("etBodies/ETVetting_Submit.json")))

      .exec(http("XUI_ET_Vetting_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.userDetails)
    }

    .pause(Environment.constantthinkTime)

  }
}