package scenarios.iac.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui.Headers

object CompleteCaseReview {

	val execute = {

		group("XUI_IAC_CompleteCaseReview_EventTrigger") {
			exec(http("XUI_IAC_CompleteCaseReview_010_GetCaseTasks")
				.get("/workallocation/case/tasks/#{caseId}/event/completeCaseReview/caseType/Asylum/jurisdiction/IA")
				.headers(Headers.commonHeader))

			.exec(Common.profile)

			.exec(http("XUI_IAC_CompleteCaseReview_010_EventTrigger")
				.get("/data/internal/cases/#{caseId}/event-triggers/completeCaseReview?ignore-warning=false")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
				.check(jsonPath("$.event_token").saveAs("eventToken")))

			.exec(Common.waJurisdictions)
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_IAC_CompleteCaseReview_Validate") {
			exec(http("XUI_IAC_CompleteCaseReview_020_Validate")
				.post("/data/case-types/Asylum/validate?pageId=completeCaseReviewcompleteCaseReview")
				.headers(Headers.commonHeader)
				.body(ElFileBody("iacBodies/IACCompleteCaseReview.json")))

			.exec(http("XUI_IAC_CompleteCaseReview_020_GetTask")
				.get("/workallocation/task/#{taskId}")
				.headers(Headers.commonHeader))
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_IAC_CompleteCaseReview_Submit") {
			exec(http("XUI_IAC_CompleteCaseReview_030_SubmitEvent")
				.post("/data/cases/#{caseId}/events")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
				.header("content-type", "application/json")
				.header("experimental", "true")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(ElFileBody("iacBodies/IACCompleteCaseReviewSubmit.json"))
				.check(substring("You have completed the case review")))

			.exec(http("XUI_IAC_CompleteCaseReview_030_CompleteTask")
				.post("/workallocation/task/#{taskId}/complete")
				.headers(Headers.commonHeader)
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(StringBody("""{"actionByEvent":true,"eventName":"Complete case review"}""")))

			.exec(http("XUI_IAC_CompleteCaseReview_030_ViewCase")
				.get("/data/internal/cases/#{caseId}")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

			.exec(Common.waJurisdictions)
			.exec(Common.waJurisdictions)
			.exec(Common.manageLabellingRoleAssignment)

			.exec(http("XUI_IAC_CompleteCaseReview_030_GetTask")
				.get("/workallocation/task/#{taskId}")
				.headers(Headers.commonHeader))
		}
	}

}
