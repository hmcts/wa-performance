package scenarios.et

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.et.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskET {

  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
  val feedETUserData = csv("ETUserData.csv").circular
  val debugMode = System.getProperty("debug", "off")

  val execute = {

    feed(feedETUserData)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{email}", "#{password}"))
    .exec(SearchCase.execute)
    .exec(_.set("taskName", "Et1Vetting"))
    .exec(ViewCase.execute)
    .feed(randomFeeder)
    .doIfOrElse(session => if (debugMode == "off") session("cancel-percentage").as[Int] < completePercentage else true) {
      exec(AssignTask.execute)
      .exec(Vetting.execute)
      .exec(PreAcceptance.execute)
    }
    {
      exec(CancelTask.execute)
    }
      .exec(XuiHelper.Logout)
  }
}