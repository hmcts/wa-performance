package scenarios.st

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.st.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskST {

  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
  val feedSTUserData = csv("STUserData.csv").circular
  val debugMode = System.getProperty("debug", "off")

  val execute = {

      feed(feedSTUserData)
      .exec(XuiHelper.Homepage)
      .exec(XuiHelper.Login("#{user}", "#{password}"))
      .exec(SearchCase.execute)
      .exec(_.set("taskName", "registerNewCase"))
      .exec(ViewCase.execute)
      .feed(randomFeeder)
      .doIfOrElse(session => if (debugMode == "off") session("complete-percentage").as[Int] < completePercentage else true) {
        exec(AssignTask.execute)
        .exec(EditCase.execute)
        .pause(30)
        .exec(_.remove("taskId"))
        .exec(_.set("taskName", "vetNewCaseDocuments"))
        .exec(ViewCase.execute)
        .exec(AssignTask.execute)
        .exec(BuildCase.execute)
      }
      {
        exec(CancelTask.execute)
      }
      .exec(XuiHelper.Logout)
  }

}