package scenarios.pcs

import ccd._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.api.payments

object CreateTaskPCS {

	val feedPCSUserData = csv("PCSSolicitorUserData.csv").circular
	val feedPCSCWUserData = csv("PCSCWUserData.csv").circular

	val execute = {

		feed(feedPCSUserData)

		.exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.PCS_PCS, "createPossessionClaim", "pcsBodies/PCSCreateCase.json"))
		.feed(feedPCSCWUserData)
		.exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("TenancyDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("TenancyDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("NoticeDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("NoticeDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("RentArrearsDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("RentArrearsDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("RentStatementDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("RentStatementDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("TenancyAgreementDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("TenancyAgreementDocumentHash")
		)))
		.feed(feedPCSUserData)
		.exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PCS_PCS.copy(microservice = "pcs_api"), "#{caseId}", "resumePossessionClaim", "pcsBodies/PCSSubmitClaim.json"))
		.exec(payments.AddPCSPayment)
//		.feed(feedPCSCWUserData)
//		.exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PCS_PCS, "#{caseId}", "changeCaseState", "pcsBodies/PCSChangeState.json"))
	}


}
