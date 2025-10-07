package scenarios.sscs

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.sscs.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskSSCS {

  val feedSSCSUserData = csv("SSCSUserData.csv").circular
  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
  val debugMode = System.getProperty("debug", "off")

  val execute =

    feed(feedSSCSUserData)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{email}", "#{password}"))
    .exec(SearchCase.execute)
    .exec(_.set("taskName", "**TBC**")) // SSCS currently not onboarded, so not able to retrieve the task name returned yet (September 2025)
    .exec(ViewCase.execute)
    .feed(randomFeeder)
    .doIfOrElse(session => if (debugMode == "off") session("complete-percentage").as[Int] < completePercentage else true) {
      exec(AssignTask.execute)
      .exec(ReviewAdminAction.execute)
    }
    {
      exec(CancelTask.execute)
    }
    .exec(XuiHelper.Logout)
}