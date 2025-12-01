package scenarios.prl

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.prl.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskPRL {

  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
  val feedPRLTribunalUsers = csv("PRLTribunalUserData.csv").circular
  val debugMode = System.getProperty("debug", "off")

  val execute = {

    feed(feedPRLTribunalUsers)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{user}", "#{password}"))
    .exec(SearchCase.execute)
    .exec(_.set("taskName", "checkApplicationFL401"))
    .exec(ViewCase.execute)
    .feed(randomFeeder)
    .doIfOrElse(session => if (debugMode == "off") session("complete-percentage").as[Int] < completePercentage else true) {
      exec(AssignTask.execute)
      .exec(AddCaseNumber.execute)
      .exec(_.remove("taskId"))
      .exec(_.set("taskName", "sendToGateKeeperFL401"))
      .exec(ViewCase.execute)
      .exec(SendToGatekeeper.execute)
    }
    {
      exec(CancelTask.execute)
    }
    .exec(XuiHelper.Logout)
  }
}