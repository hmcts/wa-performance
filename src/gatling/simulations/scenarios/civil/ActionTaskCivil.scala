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

    val execute =

      feed(feedCivilJudgeData)
      .exec(XuiHelper.Homepage)
      .exec(XuiHelper.Login("#{email}", "#{password}"))
      .exec(AllWork.allWorkTasks)
      .exec(AllWork.allWorkTasksHighPriority)
      .exec(SearchCase.execute)
      .doIf(session => session("accessRequired").as[String].equals("CHALLENGED")) {
        exec(ChallengedAccess.JudicialChallengedAccess)
      }
      .exec(_.set("taskName", "summaryJudgmentDirections"))
      .exec(SearchCase.execute)
      .exec(ViewCase.execute)
      .feed(randomFeeder)
      .doIfOrElse(session => if (debugMode == "off") session("complete-percentage").as[Int] < completePercentage else true) {
        exec(AssignTask.execute)
        .exec(StandardDirectionOrder.execute)
      }
      {
        exec(CancelTask.execute)
      }
      .exec(XuiHelper.Logout)

}