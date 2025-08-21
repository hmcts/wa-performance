package scenarios.et

import ccd._
import io.gatling.core.Predef._
import utils._

import scala.util.Random

object CreateTaskET {

  val feedETUserData = csv("ETUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute = {

    feed(feedETUserData)
    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.EMPLOYMENT_EnglandWales, "et1ReppedCreateCase", "etBodies/ET1CreateCase.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.EMPLOYMENT_EnglandWales, "#{caseId}", "et1SectionOne", "etBodies/ET1ClaimantDetails.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.EMPLOYMENT_EnglandWales, "#{caseId}", "et1SectionTwo", "etBodies/ET1EmploymentDetails.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.EMPLOYMENT_EnglandWales, "#{caseId}", "et1SectionThree", "etBodies/ET1ClaimDetails.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.EMPLOYMENT_EnglandWales, "#{caseId}", "submitEt1Draft", "etBodies/ET1SubmitClaim.json"))
  }
}