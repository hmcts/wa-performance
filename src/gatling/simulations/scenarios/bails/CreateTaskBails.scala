package scenarios.bails

import ccd._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.DateUtils

import scala.util.Random

object CreateTaskBails {

  val feedIACUserData = csv("IACUserData.csv").circular
  val feedIACAdminData = csv("UserDataBailsAdmin.csv").circular
  val feedIACHoData = csv("UserDataBailsHo.csv").circular
  val UserFeederBailsJudge = csv("UserDataBailsJudge.csv").circular
  val CaseWorkerUserFeeder = csv("CWUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    exec(_.setAll("randomString" -> randomString(5),
      "dob" -> DateUtils.getDatePastRandom("yyyy-MM-dd", minYears = 20, maxYears = 50),
      "currentDate" -> DateUtils.getDateNow("yyyy-MM-dd")))

      .feed(feedIACUserData)
      .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "startApplication", "bailsBodies/BailsCreateCase.json"))
      .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "#{caseId}", "submitApplication", "bailsBodies/BailsSubmitAppeal.json"))
      .feed(feedIACAdminData)
      .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "#{caseId}", "confirmDetentionLocation", "bailsBodies/ConfirmDetentionLocationSubmit.json"))
      .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "#{caseId}", "caseListing", "bailsBodies/BailsListCase.json"))
      .feed(feedIACHoData)
      .exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "120KB.pdf", additionalChecks = Seq(
          jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL"))))
      .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "#{caseId}", "uploadBailSummary", "bailsBodies/BailsUploadSummary.json"))
      .feed(UserFeederBailsJudge)
      .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "#{caseId}", "recordTheDecision", "bailsBodies/BailsJudgeRecordDecision.json"))
      .exec(CcdHelper.uploadDocumentToCdam("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "CaseSummary.pdf", additionalChecks = Seq(
            jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL"))))
      .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "#{caseId}", "uploadSignedDecisionNotice", "bailsBodies/BailsUploadSignedNotice.json"))

}
