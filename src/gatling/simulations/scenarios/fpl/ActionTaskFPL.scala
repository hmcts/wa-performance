package scenarios.fpl

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.fpl.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskFPL {

  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
  val feedFPLUserData = csv("WA_FPLCTSCUsers.csv").circular
  val debugMode = System.getProperty("debug", "off")

  val execute = {

    feed(feedFPLUserData)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{email}", "#{password}"))
    .exec(SearchCase.execute)
    .exec(_.set("taskName", "reviewMessageHearingCentreAdmin"))
    .exec(ViewCase.execute)
    .feed(randomFeeder)
    .doIfOrElse(session => if (debugMode == "off") session("complete-percentage").as[Int] < completePercentage else true) {
      exec(AssignTask.execute)
      .exec(ReplyToMessage.execute)
    }
    {
      exec(CancelTask.execute)
    }
      .exec(XuiHelper.Logout)
  }
}
