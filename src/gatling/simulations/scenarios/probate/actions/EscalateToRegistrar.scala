package scenarios.probate.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import xui.Headers

object EscalateToRegistrar {
	
	val execute = {

		group("XUI_Probate_EscalateToRegistrar_EventTrigger") {
			exec(http("XUI_Probate_EscalateToRegistrar_010_GetCaseTasks")
				.get("/workallocation/case/tasks/#{caseId}/event/boEscalateToRegistrar/caseType/GrantOfRepresentation/jurisdiction/PROBATE")
				.headers(Headers.commonHeader)
				.header("accept", "application/json, text/plain, */*"))

			.exec(Common.waJurisdictions)

			.exec(http("XUI_Probate_EscalateToRegistrar_010_EventTrigger")
				.get("/data/internal/cases/#{caseId}/event-triggers/boEscalateToRegistrar?ignore-warning=false")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
				.check(jsonPath("$.event_token").saveAs("eventToken")))

			.exec(Common.waJurisdictions)
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_Probate_EscalateToRegistrar_Validate") {
			exec(http("XUI_Probate_EscalateToRegistrar_020_Validate")
				.post("/data/case-types/GrantOfRepresentation/validate?pageId=boEscalateToRegistrarregistrarEscalateReasonPage1")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(ElFileBody("probateBodies/ProbateEscalateToRegistrarValidate.json")))

			.exec(Common.waJurisdictions)
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_Probate_EscalateToRegistrar_Submit") {
			exec(http("XUI_Probate_EscalateToRegistrar_030_GetTask")
				.get("/workallocation/task/#{taskId}")
				.headers(Headers.commonHeader))

				.exec(http("XUI_Probate_EscalateToRegistrar_030_SubmitEvent")
					.post("/data/cases/#{caseId}/events")
					.headers(Headers.commonHeader)
					.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
					.header("x-xsrf-token", "#{XSRFToken}")
					.body(ElFileBody("probateBodies/ProbateEscalateToRegistrarSubmit.json")))

				.exec(http("XUI_Probate_EscalateToRegistrar_030_CompleteTask")
					.post("/workallocation/task/#{taskId}/complete")
					.headers(Headers.commonHeader)
					.header("x-xsrf-token", "#{XSRFToken}")
					.body(StringBody("{}")))

				.exec(http("XUI_Probate_EscalateToRegistrar_030_ViewCase")
					.get("/data/internal/cases/#{caseId}")
					.headers(Headers.commonHeader)
					.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

				.exec(Common.waJurisdictions)
				.exec(Common.manageLabellingRoleAssignment)

				.exec(http("XUI_Probate_EscalateToRegistrar_030_PostCaseTasks")
					.post("/workallocation/case/task/#{caseId}")
					.headers(Headers.commonHeader)
					.header("x-xsrf-token", "#{XSRFToken}")
					.header("accept", "application/json, text/plain, */*")
					.body(StringBody("""{"refined":true}""")))
		}
	}
}