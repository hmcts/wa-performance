package scenarios.wa

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.prl.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskWA {

  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
  val feedWAUserData = csv("WATestUserData.csv").circular
  val debugMode = System.getProperty("debug", "off")

  val execute = {

    feed(feedWAUserData)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{user}", "#{password}"))
    .exec(SearchCase.execute)
    .exec(XuiHelper.Logout)
  }

}