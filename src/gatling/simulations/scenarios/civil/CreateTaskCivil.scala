package scenarios.civil

import ccd._
import io.gatling.core.Predef._
import scenarios.api.payments
import scenarios.civil.actions._
import utilities.DateUtils

import scala.util.Random

object CreateTaskCivil {

  val feedCivilUserData = csv("CivilUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    exec(_.setAll("todayYear" -> DateUtils.getDateNow("yyyy")))

    .feed(feedCivilUserData)
    .exec(_.set("jurisdiction", "CIVIL"))
    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.CIVIL_CIVIL, "CREATE_CLAIM", "civilBodies/CreateUnspecifiedClaim.json"))
    .pause(60)
    .exec(payments.AddCivilPayment)
    .pause(60)
    .tryMax(2) {
      exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.CIVIL_CIVIL, "#{caseId}", "NOTIFY_DEFENDANT_OF_CLAIM", "civilBodies/NotifyClaim.json"))
    }
    .pause(60)
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.CIVIL_CIVIL, "#{caseId}", "NOTIFY_DEFENDANT_OF_CLAIM_DETAILS", "civilBodies/NotifyClaimDetails.json"))
    .pause(60)
    .exec(UpdateDate.execute)
    .pause(60)
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.CIVIL_CIVIL, "#{caseId}", "DEFAULT_JUDGEMENT", "civilBodies/RequestDefaultJudgement.json"))

}