package scenarios.pcs.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object CreateNewHearing {

	val execute =

		group("XUI_PCS_CreateNewHearing_ViewTask") {
			exec(Common.isAuthenticated)

			.exec(http("XUI_PCS_CreateNewHearing_OpenTask_010_GetTask")
				.get("/workallocation/task/#{taskId}")
				.headers(Headers.commonHeader))

			.exec(http("XUI_PCS_CreateNewHearing_OpenTask_010_GetTaskRoles")
				.get("/workallocation/task/#{taskId}/roles")
				.headers(Headers.commonHeader))

			.exec(http("XUI_PCS_CreateNewHearing_OpenTask_010_GetUserByIdamId")
				.post("/workallocation/caseworker/getUserByIdamId")
				.headers(Headers.commonHeader)
				.header("accept", "application/json, text/plain, */*")
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(StringBody("""{"idamId":"#{idamId}","silentNotFound":true}""")))

			.exec(Common.waJurisdictions)

			.exec(http("XUI_PCS_CreateNewHearing_OpenTask_010_GetJudicialUsers")
				.post("/api/role-access/roles/getJudicialUsers")
				.headers(Headers.commonHeader)
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(StringBody("""{"userIds":["#{idamId}"],"services":"PCS"}""")))
		}

		.pause(Environment.constantthinkTime)

		.group("XUI_PCS_CreateNewHearing_CompleteTask") {
			exec(http("XUI_PCS_CreateNewHearing_CompleteTask_020_CompleteTask")
				.post("/workallocation/task/#{taskId}/complete")
				.headers(Headers.commonHeader)
				.header("x-xsrf-token", "#{XSRFToken}")
				.body(StringBody("""{"hasNoAssigneeOnComplete":false}""")))

				.exec(Common.isAuthenticated)
				.exec(Common.waJurisdictions)
				.exec(Common.manageLabellingRoleAssignment)
				.exec(Common.waJurisdictions)

				.exec(http("XUI_PCS_CreateNewHearing_CompleteTask_020_GetCaseTasks")
					.post("/workallocation/case/task/#{caseId}")
					.headers(Headers.commonHeader)
					.header("x-xsrf-token", "#{XSRFToken}")
					.body(StringBody("""{"refined":true}""")))

				.exec(http("XUI_PCS_CreateNewHearing_CompleteTask_020_GetUsersByIdamId")
					.post("/workallocation/caseworker/getUsersByIdamIds")
					.headers(Headers.commonHeader)
					.header("x-xsrf-token", "#{XSRFToken}")
					.body(StringBody("""{"idamIds":[],"services":["PCS"]}""")))
		}

		.pause(Environment.constantthinkTime)
}
