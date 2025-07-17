package scenarios.prl

import ccd._
import io.gatling.core.Predef._
import utils._

import scala.util.Random

object CreateTask {

  val feedPRLUserData = csv("PRLUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    feed(feedPRLUserData)

    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "solicitorCreate", "prlBodies/prlCreateCase.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "fl401TypeOfApplication", "prlBodies/prlApplicationType.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "withoutNoticeOrderDetails", "prlBodies/prlWithoutNotice.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "applicantsDetails", "prlBodies/prlApplicantDetails.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "respondentsDetails", "prlBodies/prlRespondentDetails.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "fl401ApplicantFamilyDetails", "prlBodies/prlFamilyDetails.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "respondentRelationship", "prlBodies/prlRelationship.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "respondentBehaviour", "prlBodies/prlBehaviour.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "fl401Home", "prlBodies/prlHome.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PRIVATELAW_PRLAPPS, "#{caseId}", "fl401StatementOfTruthAndSubmit", "prlBodies/prlSubmit.json"))
}
