package scenarios.probate

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.probate.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskProbate {

	val feedProbateUserData = csv("ProbateUserData.csv").circular
	val completePercentage = 90 //Percentage of Complete Tasks //90
	val randomFeeder = Iterator.continually(Map("cancel-percentage" -> Random.nextInt(100)))
	val debugMode = System.getProperty("debug", "off")

	val execute =

		feed(feedProbateUserData)
		.exec(XuiHelper.Homepage)
		.exec(XuiHelper.Login("#{email}", "#{password}"))
		.exec(SearchCase.execute)
		.exec(_.set("taskName", "ExamineDigitalCaseProbate"))
		.exec(ViewCase.execute)
		.feed(randomFeeder)
		.doIfOrElse(session => if (debugMode == "off") session("cancel-percentage").as[Int] < completePercentage else true) {
			exec(AssignTask.execute)
			.exec(EscalateToRegistrar.execute)
		}
		{
			exec(CancelTask.execute)
		}
		.exec(XuiHelper.Logout)

}