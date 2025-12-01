package scenarios.fpl

import ccd._
import io.gatling.core.Predef._
import utils._

import scala.util.Random

object CreateTaskFPL {

  val feedFPLUserData = csv("FPLUserData.csv").circular
  val feedWAFPLUserData = csv("WA_FPLCTSCUsers.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    feed(feedFPLUserData)
    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "openCase", "fplBodies/FPLCreateCase.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "#{caseId}", "ordersNeeded", "fplBodies/FPLOrdersNeeded.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "#{caseId}", "hearingNeeded", "fplBodies/FPLHearingNeeded.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "#{caseId}", "enterGrounds", "fplBodies/FPLEnterGrounds.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "#{caseId}", "enterChildren", "fplBodies/FPLEnterChildren.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "#{caseId}", "enterRespondents", "fplBodies/FPLEnterRespondents.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "#{caseId}", "otherProposal", "fplBodies/FPLOtherProposal.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "#{caseId}", "submitApplication", "fplBodies/FPLSubmitApplication.json"))
    .pause(30)
    .feed(feedWAFPLUserData)
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.PUBLICLAW_CARE_SUPERVISION_EPO, "#{caseId}", "messageJudgeOrLegalAdviser", "fplBodies/FPLSendMessage.json"))

}