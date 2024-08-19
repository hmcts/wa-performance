package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiFpl {

  val baseURL = Environment.xuiBaseURL

  val SearchCase = 

    exec(http("XUI_GlobalSearch_010_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_GlobalSearch_010_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

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

    .exec(http("XUI_GlobalSearch_020_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
		
		.exec(http("XUI_GlobalSearch_020_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .exec(http("XUI_GlobalSearch_020_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

  val ViewCase = 

    exec(http("XUI_ViewCase_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_ViewCase_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_ViewCase_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

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
      .check(jsonPath("$..[?(@.type=='reviewMessageHearingCentreAdmin')].id").saveAs("taskId")))

    .pause(Environment.constantthinkTime)

	val ReplyToMessage =

		exec(http("XUI_ReplyToMessage_GetTasks")
			.get("/case/PUBLICLAW/CARE_SUPERVISION_EPO/#{caseId}/trigger/replyToMessageJudgeOrLegalAdviser?tid=#{taskId}")
			.headers(XUIHeaders.xuiMainHeader)
			.check(substring("HMCTS Manage cases")))

		.exec(http("XUI_ReplyToMessage_ConfigurationUI")
			.get("/external/configuration-ui/")
			.headers(XUIHeaders.xuiMainHeader))

		.exec(http("XUI_ReplyToMessage_ConfigJson")
			.get("/assets/config/config.json")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json, text/plain, */*"))

		.exec(http("XUI_ReplyToMessage_T&C")
			.get("/api/configuration?configurationKey=termsAndConditionsEnabled")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json, text/plain, */*"))

		.exec(http("XUI_ReplyToMessage_ConfigUI")
			.get("/external/config/ui")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json, text/plain, */*"))

		.exec(http("XUI_ReplyToMessage_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json, text/plain, */*"))

		.exec(http("XUI_ReplyToMessage_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json, text/plain, */*"))

		.exec(http("XUI_ReplyToMessage_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
			.header("content-type", "application/json")
			.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

		.exec(http("XUI_ReplyToMessage_Profile")
			.get("/data/internal/profile")
			.headers(XUIHeaders.xuiMainHeader)
			.header("content-type", "application/json")
			.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8"))

		.exec(http("XUI_ReplyToMessage_EventTrigger")
			.get("/data/internal/cases/#{caseId}/event-triggers/replyToMessageJudgeOrLegalAdviser?ignore-warning=false")
			.headers(XUIHeaders.xuiMainHeader)
			.header("content-type", "application/json")
			.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
			.check(jsonPath("$.event_token").saveAs("eventToken")))

		.exec(http("XUI_ReplyToMessage_GetTasks")
			.get("/workallocation/case/tasks/#{caseId}/event/replyToMessageJudgeOrLegalAdviser/caseType/CARE_SUPERVISION_EPO/jurisdiction/PUBLICLAW")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json")
			.header("content-type", "application/json"))

		.exec(http("XUI_ReplyToMessage_GetTasks")
			.get("/workallocation/case/tasks/#{caseId}/event/replyToMessageJudgeOrLegalAdviser/caseType/CARE_SUPERVISION_EPO/jurisdiction/PUBLICLAW")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json")
			.header("content-type", "application/json"))

		.pause(Environment.constantthinkTime)

		.exec(http("XUI_ReplyToMessage_Page1")
			.post("/data/case-types/CARE_SUPERVISION_EPO/validate?pageId=replyToMessageJudgeOrLegalAdviserSelectMessage")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
			.header("content-type", "application/json")
			.header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/FPLSendMessage1.json")))

		.exec(http("XUI_ReplyToMessage_Page1ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json, text/plain, */*"))

		.pause(Environment.constantthinkTime)

		.exec(http("XUI_ReplyToMessage_Page2")
			.post("/data/case-types/CARE_SUPERVISION_EPO/validate?pageId=replyToMessageJudgeOrLegalAdviserReplyToMessage")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
			.header("content-type", "application/json")
			.header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/FPLSendMessage2.json")))

		.exec(http("XUI_ReplyToMessage_Page1ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json, text/plain, */*"))

		.pause(Environment.constantthinkTime)

		.exec(http("XUI_ReplyToMessage_Submit")
			.post("/data/cases/#{caseId}/events")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
			.header("content-type", "application/json")
			.header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/FPLSendMessageComplete.json")))

		.exec(http("XUI_ReplyToMessage_CompleteTask")
			.post("/workallocation/task/#{taskId}/complete")
			.header("accept", "application/json")
			.header("content-type", "application/json")
			.header("x-xsrf-token", "#{xsrfToken}"))

		.exec(http("XUI_ReplyToMessage_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
			.header("content-type", "application/json")
			.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

		.exec(http("XUI_ReplyToMessage_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(XUIHeaders.xuiMainHeader)
			.header("accept", "application/json, text/plain, */*"))

		.exec(http("XUI_ReplyToMessage_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))

		.pause(Environment.constantthinkTime)

}