package scenarios.iac

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.iac.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskIAC {

  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
  val feedTribunalUserData = csv("IAStaffUserData.csv").circular
  val debugMode = System.getProperty("debug", "off")

  val execute =

    feed(feedTribunalUserData)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{user}", "#{password}"))
    .exec(SearchCase.execute)
    .exec(_.set("taskName", "reviewTheAppeal"))
    .exec(ViewCase.execute)
    .feed(randomFeeder)
    .doIfOrElse(session => if (debugMode == "off") session("complete-percentage").as[Int] < completePercentage else true) {
      exec(AssignTask.execute)
      .exec(RequestRespondentEvidence.execute)
    }
    {
      exec(CancelTask.execute)
    }
    .exec(XuiHelper.Logout)

}