package scenarios.pcs

import ccd._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.api.payments
import utilities._

object CreateTaskPCS {

	val feedPCSUserData = csv("PCSSolicitorUserData.csv").circular
	val feedPCSCWUserData = csv("PCSCWUserData.csv").circular

	val execute = {

		feed(feedPCSUserData)
			.exec(session => session
				.set("solicitorEmail", session("email").as[String])
				.set("solicitorPassword", session("password").as[String]))

		.exec(CcdHelper.createCase("#{solicitorEmail}", "#{solicitorPassword}", CcdCaseTypes.PCS_PCS, "createPossessionClaim", "pcsBodies/PCSCreateCase.json"))
		.feed(feedPCSCWUserData)
			.exec(session => session
				.set("cwEmail", session("email").as[String])
				.set("cwPassword", session("password").as[String]))

		.exec(CcdHelper.uploadDocumentToCdam("#{cwEmail}", "#{cwPassword}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("TenancyDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("TenancyDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{cwEmail}", "#{cwPassword}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("NoticeDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("NoticeDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{cwEmail}", "#{cwPassword}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("RentArrearsDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("RentArrearsDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{cwEmail}", "#{cwPassword}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("RentStatementDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("RentStatementDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{cwEmail}", "#{cwPassword}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("TenancyAgreementDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("TenancyAgreementDocumentHash")
		)))
		.exec(CcdHelper.addCaseEvent("#{solicitorEmail}", "#{solicitorPassword}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "#{caseId}", "resumePossessionClaim", "pcsBodies/PCSSubmitClaim.json"))
		.exec(payments.AddPCSPayment)
		.exec(_.set("pastDate", DateUtils.getDatePast("yyyy-MM-dd", days = 10)))
		.exec(CcdHelper.addCaseEvent("#{cwEmail}", "#{cwPassword}", CcdCaseTypes.PCS_PCS, "#{caseId}", "addCaseReviewDate", "pcsBodies/PCSAddReviewDate.json"))
	}


}
