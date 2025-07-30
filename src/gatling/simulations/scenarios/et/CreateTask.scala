package scenarios.et

import ccd._
import io.gatling.core.Predef._
import utilities.DateUtils
import utils._

import scala.util.Random

object CreateTask {

  val feedETUserData = csv("ETUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute = {

    exec(_.setAll(  "firstName"  -> ("Perf" + Common.randomString(5)),
      "lastName"  -> ("Test" + Common.randomString(5)),
      "dob" -> DateUtils.getDatePastRandom("yyyy-MM-dd", minYears = 20, maxYears = 50),
      "todayDate" -> Common.getDate(),
      "todayYear" -> Common.getYear()))

    feed(feedETUserData)
    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.EMPLOYMENT_EnglandWales, "et1ReppedCreateCase", "etBodies/ETCreateCase.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.EMPLOYMENT_EnglandWales, "#{caseId}", "submitEt1Draft", "etBodies/ETSubmitDraft.json"))
  }


}
