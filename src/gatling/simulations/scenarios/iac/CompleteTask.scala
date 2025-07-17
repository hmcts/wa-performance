package scenarios.iac

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.iac.actions._
import xui.XuiHelper

import scala.util.Random

object CompleteTask {

  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("cancel-percentage" -> Random.nextInt(100)))
  val feedTribunalUserData = csv("WA_TribunalUsers.csv").circular

  val execute = {

    feed(feedTribunalUserData)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{user}", "#{password}"))
//    .exec(SearchCase.execute)
    .exec(_.set("taskName", "reviewTheAppeal"))
    .exec(ViewCase.execute)
    .feed(randomFeeder)
    .doIfOrElse(session => session("cancel-percentage").as[Int] < completePercentage) {
      exec(AssignTask.execute)
      .exec(RequestRespondentEvidence.execute)
    }
    {
      exec(CancelTask.execute)
    }
    .exec(XuiHelper.Logout)
  }

}