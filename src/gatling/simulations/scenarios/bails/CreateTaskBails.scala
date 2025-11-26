package scenarios.bails

import ccd._
import io.gatling.core.Predef._
import utilities.DateUtils

import scala.util.Random

object CreateTaskBails {

  val feedIACUserData = csv("IACUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    exec(_.setAll("randomString" -> randomString(5),
      "dob" -> DateUtils.getDatePastRandom("yyyy-MM-dd", minYears = 20, maxYears = 50)))

      .feed(feedIACUserData)

      .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "startApplication", "bailsBodies/BailsCreateCase.json"))
      .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Bail, "#{caseId}", "submitApplication", "bailsBodies/BailsSubmitAppeal.json"))

}
