package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiFpl {

  val baseURL = Environment.xuiBaseURL

  val SearchCase = 

    group("XUI_GlobalSearch_Request") {
      exec(Common.isAuthenticated)
      .exec(Common.apiUserDetails)

      .exec(http("XUI_GlobalSearch_010_Services")
        .get("/api/globalSearch/services")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_GlobalSearch_010_JurisdictionsRead")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .pause(Environment.constantthinkTime)

      .exec(http("XUI_GlobalSearch_020_Request")
        .post("/api/globalsearch/results")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/FPLCaseSearch.json")))

      .exec(Common.isAuthenticated)
      
      .exec(http("XUI_GlobalSearch_020_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

  val ViewCase = 

    exec(Common.isAuthenticated)
    .exec(Common.waSupportedJurisdictions)
    .exec(Common.apiUserDetails)

    .exec(http("XUI_ViewCase_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
			.check(jsonPath("$.tabs[?(@.id=='JudicialMessagesTab')].fields[?(@.id=='judicialMessages')].value[0].id").saveAs("messageId"))
			.check(jsonPath("$.tabs[?(@.id=='JudicialMessagesTab')].fields[?(@.id=='judicialMessages')].value[0].value.requestedBy").saveAs("messageFrom"))
			.check(jsonPath("$.tabs[?(@.id=='JudicialMessagesTab')].fields[?(@.id=='judicialMessages')].value[0].value.dateSent").saveAs("messageDate")))

    .exec(http("XUI_ViewCase_GetWorkAllocationTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(XUIHeaders.xuiMainHeader)
      .check(jsonPath("$..[?(@.type=='reviewMessageHearingCentreAdmin')].id").optional.saveAs("taskId"))) //reviewResponseAllocatedJudge

    //Save taskType from response
    .exec(session => {
      // Initialise task type in session if it's not already present, ensure the variable exists before entering Loop
      session("taskType").asOption[String] match {
        case Some(taskType) => session
        case None => session.set("taskType", "")
      }
    })

    // Loop until the task type matches "reviewMessageHearingCentreAdmin"
    .asLongAs(session => session("taskType").as[String] != "reviewMessageHearingCentreAdmin") {
      exec(http("XUI_ST_SelectCaseTaskRepeat")
        .get("/workallocation/case/task/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{xsrfToken}")
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

	val ReplyToMessage =

    group("XUI_FPL_ReplyToMessage_Start") {
      exec(http("XUI_FPL_ReplyToMessage_GetTasks")
        .get("/case/PUBLICLAW/CARE_SUPERVISION_EPO/#{caseId}/trigger/replyToMessageJudgeOrLegalAdviser?tid=#{taskId}")
        .headers(XUIHeaders.xuiMainHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.apiUserDetails)
      .exec(Common.isAuthenticated)

      .exec(http("XUI_FPL_ReplyToMessage_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.profile)

      .exec(http("XUI_FPL_ReplyToMessage_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/replyToMessageJudgeOrLegalAdviser?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(http("XUI_FPL_ReplyToMessage_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/replyToMessageJudgeOrLegalAdviser/caseType/CARE_SUPERVISION_EPO/jurisdiction/PUBLICLAW")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json")
        .header("content-type", "application/json"))

      .exec(http("XUI_FPL_ReplyToMessage_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/replyToMessageJudgeOrLegalAdviser/caseType/CARE_SUPERVISION_EPO/jurisdiction/PUBLICLAW")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json")
        .header("content-type", "application/json"))
    }

		.pause(Environment.constantthinkTime)

    .group("XUI_FPL_ReplyToMessage_Page1") {
      exec(http("XUI_FPL_ReplyToMessage_Page1")
        .post("/data/case-types/CARE_SUPERVISION_EPO/validate?pageId=replyToMessageJudgeOrLegalAdviserSelectMessage")
        .headers(XUIHeaders.xuiMainHeader)
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
        .headers(XUIHeaders.xuiMainHeader)
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
        .headers(XUIHeaders.xuiMainHeader)
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
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.waJurisdictions)
      .exec(Common.apiUserDetails)
    }
}