package scenarios.civil

import io.gatling.core.Predef._
import scenarios.civil.actions._
import scenarios.common.wa._
import scenarios.common.xui._
import xui.XuiHelper

import scala.util.Random

object ActionTaskCivil {

    val completePercentage = 90 //Percentage of Complete Tasks //90
    val randomFeeder = Iterator.continually(Map("complete-percentage" -> Random.nextInt(100)))
    val feedCivilJudgeData = csv("CivilJudicialUserData.csv").circular
    val debugMode = System.getProperty("debug", "off")

    val execute = {

      feed(feedCivilJudgeData)
      .exec(XuiHelper.Homepage)
  }

}
