package scenarios.probate

import ccd._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities._

object CreateTaskProbate {

	val feedProbateUserData = csv("ProbateUserData.csv").circular

	val execute = {

		feed(feedProbateUserData)
		.exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.PROBATE_GrantOfRepresentation, "applyforGrantPaperApplicationMan", "probateBodies/ProbateCreateCase.json"))
	}
}
