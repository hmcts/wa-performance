package scenarios.iac

import ccd._
import io.gatling.core.Predef._
import utilities.DateUtils

import scala.util.Random

object CreateTask {

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    exec(_.setAll("randomString" -> randomString(5),
                  "dob" -> DateUtils.getDatePastRandom("yyyy-MM-dd", minYears = 20, maxYears = 50),
                  "todayDate" -> DateUtils.getDateNow("yyyy-MM-dd")))

    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "startAppeal", "iacBodies/IACCreateCase.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "#{caseId}", "submitAppeal", "iacBodies/IACSubmitAppeal.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "#{caseId}", "requestHomeOfficeData", "iacBodies/IACRequestHomeOfficeData.json"))


}
