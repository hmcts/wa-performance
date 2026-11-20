package scenarios.fr.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import xui.Headers

object AttachScannedDocument {

	val execute = {

		group("XUI_FRConsented_AttachScannedDocument_EventTrigger") {
			exec(http("XUI_FRConsented_AttachScannedDocument_010_GetCaseTasks")
				.get("/workallocation/case/tasks/#{caseId}/event/attachScannedDocs/caseType/FinancialRemedyMVP2/jurisdiction/DIVORCE")
				.headers(Headers.commonHeader))

			.exec(http("XUI_FRConsented_AttachScannedDocument_010_EventTrigger")
				.get("/data/internal/cases/#{caseId}/event-triggers/attachScannedDocs?ignore-warning=false")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
				.header("content-type", "application/json")
				.check(jsonPath("$.event_token").saveAs("eventToken")))

			.exec(Common.waSupportedJurisdictions)
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_FRConsented_AttachScannedDocument_Validate1") {
			exec(http("XUI_FRConsented_AttachScannedDocument_020_Validate1")
				.post("/data/case-types/FinancialRemedyMVP2/validate?pageId=attachScannedDocs1")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
				.header("content-type", "application/json")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(ElFileBody("frBodies/consented/FRAttachScannedDocsValidate1.json")))

			.exec(Common.waSupportedJurisdictions)
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_FRConsented_AttachScannedDocument_Validate2") {
			exec(http("XUI_FRConsented_AttachScannedDocument_030_Validate2")
				.post("/data/case-types/FinancialRemedyMVP2/validate?pageId=attachScannedDocs2")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
				.header("content-type", "application/json")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(ElFileBody("frBodies/consented/FRAttachScannedDocsValidate2.json")))

			.exec(Common.waSupportedJurisdictions)

			.exec(http("XUI_FRConsented_AttachScannedDocument_030_GetTask")
				.get("/workallocation/task/#{taskId}")
				.headers(Headers.commonHeader)
				.header("content-type", "application/json"))
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_FRConsented_AttachScannedDocument_Submit") {
			exec(http("XUI_FRConsented_AttachScannedDocument_040_Submit")
				.post("/data/cases/#{caseId}/events")
				.headers(Headers.commonHeader)
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
				.header("content-type", "application/json")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(ElFileBody("frBodies/consented/FRAttachScannedDocsSubmit.json")))

			.exec(http("XUI_FRConsented_AttachScannedDocument_040_CompleteTask")
				.post("/workallocation/task/#{taskId}/complete")
				.headers(Headers.commonHeader)
				.header("content-type", "application/json")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(StringBody("{}")))

			.exec(http("XUI_FRConsented_AttachScannedDocument_040_ViewCase")
				.get("/data/internal/cases/#{caseId}")
				.headers(Headers.commonHeader)
				.header("content-type", "application/json")
				.header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

			.exec(Common.waSupportedJurisdictions)

			.exec(Common.manageLabellingRoleAssignment)

			.exec(http("XUI_FRConsented_AttachScannedDocument_040_PostTask")
				.post("/workallocation/case/task/#{caseId}")
				.headers(Headers.commonHeader)
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(StringBody("""{"refined":true}""")))
		}

		.pause(Environment.constantthinkTime)

	}

}