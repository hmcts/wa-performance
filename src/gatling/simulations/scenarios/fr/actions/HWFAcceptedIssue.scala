package scenarios.fr.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import xui.Headers

object HWFAcceptedIssue {

	val execute =

		group("XUI_FRConsented_HWFAcceptedAndIssue_EventTrigger") {
			exec(http("XUI_FRConsented_HWFAcceptedAndIssue_010_GetCaseTasks")
				.get("/workallocation/case/tasks/#{caseId}/event/FR_HWFAcceptedAndIssue/caseType/FinancialRemedyMVP2/jurisdiction/DIVORCE")
				.headers(Headers.commonHeader))

			.exec(http("XUI_FRConsented_HWFAcceptedAndIssue_010_EventTrigger")
				.get("/data/internal/cases/#{caseId}/event-triggers/FR_HWFAcceptedAndIssue?ignore-warning=false")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
				.header("content-type", "application/json")
				.check(jsonPath("$.event_token").saveAs("eventToken")))

			.exec(Common.profile)

			.exec(Common.waSupportedJurisdictions)
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_FRConsented_HWFAcceptedAndIssue_Validate") {
			exec(http("XUI_FRConsented_HWFAcceptedAndIssue_020_Validate")
				.post("/data/case-types/FinancialRemedyMVP2/validate?pageId=FR_HWFAcceptedAndIssueupdateIssueDate")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
				.header("content-type", "application/json")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(ElFileBody("frBodies/consented/FRHWFAcceptedIssueValidate.json")))

			.exec(Common.waSupportedJurisdictions)

			.exec(http("XUI_FRConsented_HWFAcceptedAndIssue_020_GetTask")
				.get("/workallocation/task/#{taskId}")
				.headers(Headers.commonHeader)
				.header("content-type", "application/json"))
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_FRConsented_HWFAcceptedAndIssue_Submit") {
			exec(http("XUI_FRConsented_HWFAcceptedAndIssue_030_SubmitEvent")
				.post("/data/cases/#{caseId}/events")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
				.header("content-type", "application/json")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(ElFileBody("frBodies/consented/FRHWFAcceptedIssueSubmit.json")))

			.exec(http("XUI_FRConsented_HWFAcceptedAndIssue_030_CompleteTask")
				.post("/workallocation/task/#{taskId}/complete")
				.headers(Headers.commonHeader)
				.header("content-type", "application/json")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(StringBody("{}")))

			.exec(http("XUI_FRConsented_HWFAcceptedAndIssue_030_ViewCase")
				.get("/data/internal/cases/#{caseId}")
				.headers(Headers.commonHeader)
				.header("content-type", "application/json")
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

			.exec(Common.waSupportedJurisdictions)

			.exec(Common.manageLabellingRoleAssignment)

			.exec(http("XUI_FRConsented_HWFAcceptedAndIssue_030_PostTasks")
				.post("/workallocation/case/task/#{caseId}")
				.headers(Headers.commonHeader)
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(StringBody("""{"refined":true}""")))
		}

		.pause(Environment.constantthinkTime)

}