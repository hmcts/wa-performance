package scenarios.fr

import ccd._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.DateUtils

object CreateTaskFRConsented {

	val feedFRUserData = csv("FRUserData.csv").circular
	val feedFRCWData = csv("FRCWUserData.csv").circular

	val execute =

		exec(_.set("currentDate", DateUtils.getDateNow("yyyy-MM-dd")))

		.feed(feedFRUserData)
		.exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.DIVORCE_FinancialRemedyMVP2, "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("divorceUploadEvidence2DocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("divorceUploadEvidence2DocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.DIVORCE_FinancialRemedyMVP2, "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("consentOrderDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("consentOrderDocumentHash")
		)))
		.exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.DIVORCE_FinancialRemedyMVP2, "1MB.pdf", additionalChecks = Seq(
			jsonPath("$.documents[0]._links.self.href").saveAs("d81JointDocumentURL"),
			jsonPath("$.documents[0].hashToken").saveAs("d81JointDocumentHash")
		)))
		.exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.DIVORCE_FinancialRemedyMVP2, "FR_solicitorCreate", "frBodies/consented/FRSubmitApplication.json"))
		.exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.DIVORCE_FinancialRemedyMVP2, "#{caseId}", "FR_applicationPaymentSubmission", "frBodies/consented/FRCaseSubmission.json"))
		.feed(feedFRCWData)
		.exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.DIVORCE_FinancialRemedyMVP2, "#{caseId}", "handleEvidence", "frBodies/consented/FRHandleSupplementaryEvidence.json"))
}
