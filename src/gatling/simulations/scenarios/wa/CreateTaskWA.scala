package scenarios.wa

import ccd._
import io.gatling.core.Predef._
import scenarios.wa.actions._
import utils.Environment

import scala.util.Random

object CreateTaskWA {

  val feedWAUserData = csv("WATestUserData.csv").circular
  val feedWATaskTypes = csv("WA_TaskTypes.csv").random

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val execute =

    feed(feedWAUserData)
    .exitBlockOnFail {
      exec(CcdHelper.createCase("#{user}", "#{password}", CcdCaseTypes.WA_WaCaseType, "CREATE", "waBodies/WACreateCase.json"))
      .pause(Environment.constantthinkTime)
      .repeat(1, "counter") {
        feed(feedWATaskTypes)
        .exec(Camunda.PostCaseTaskAttributes)
        .exec(TaskManagement.PostTask)
      }
    }
}