package scenarios.wa

import ccd._
import io.gatling.core.Predef._
import scenarios.wa.actions._

import scala.util.Random

object CreateTaskWA {

  val feedWAUserData = csv("WATestUserData.csv").circular

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    feed(feedWAUserData)

    .exec(CcdHelper.createCase("#{user}", "#{password}", CcdCaseTypes.WA_WaCaseType, "CREATE", "waBodies/WACreateCase.json"))
    .exec(Camunda.PostCaseTaskAttributes)
    .exec(TaskManagement.PostTask)


}
