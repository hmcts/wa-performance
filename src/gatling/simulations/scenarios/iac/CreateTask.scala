package scenarios.iac

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import utils._
import ccd._
import scala.util.Random
import utilities.DateUtils

object CreateTask {

  val feedIACUserData = csv("IACUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    exec(_.setAll("randomString" -> randomString(5),
      "dob" -> DateUtils.getDatePastRandom("yyyy-MM-dd", minYears = 20, maxYears = 50),
      "todayDate" -> DateUtils.getDateNow("yyyy-MM-dd")))

    .feed(feedIACUserData)

    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "startAppeal", "iacBodies/IACCreateCase.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "#{caseId}", "submitAppeal", "iacBodies/IACSubmitAppeal.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "#{caseId}", "requestHomeOfficeData", "iacBodies/IACRequestHomeOfficeData.json"))


}
