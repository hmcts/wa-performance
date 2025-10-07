package scenarios.sscs

import ccd._
import io.gatling.core.Predef._
import utilities.DateUtils
import utils.Environment

import scala.util.Random

object CreateTaskSSCS {

  val feedSSCSUserData = csv("SSCSUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def randomNumber(length: Int) = {
    rnd.alphanumeric.filter(_.isDigit).take(length).mkString
  }

  val execute =

    exec(_.setAll(
      "NINumber" -> randomNumber(8),
      "firstname"  -> ("Perf" + randomString(5)),
      "lastname"  -> ("Test" + randomString(5)),
      "todayDate" -> DateUtils.getDateNow("yyyy-MM-dd")))

    .feed(feedSSCSUserData)
    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.SSCS_Benefit, "validAppealCreated", "sscsBodies/SSCSCreateAppeal.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.SSCS_Benefit, "#{caseId}", "sendToAdmin", "sscsBodies/SSCSSendToAdmin.json"))
    .pause(Environment.constantthinkTime)
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.SSCS_Benefit, "#{caseId}", "addHearing", "sscsBodies/SSCSDirectionIssued.json"))
}