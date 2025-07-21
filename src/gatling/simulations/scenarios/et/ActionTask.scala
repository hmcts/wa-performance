package scenarios.et

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.prl.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTask {

  val completePercentage = 90 //Percentage of Complete Tasks //90
  val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
  val feedETUserData = csv("ETUserData.csv").circular
  val debugMode = System.getProperty("debug", "off")

  val execute = {

      feed(feedETUserData)
      .exec(XuiHelper.Homepage)
      .exec(XuiHelper.Login("#{user}", "#{password}"))

  }
}