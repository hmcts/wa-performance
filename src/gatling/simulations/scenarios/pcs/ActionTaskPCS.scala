package scenarios.pcs

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.pcs.actions._
import xui.XuiHelper

import scala.util.Random

object ActionTaskPCS {

	val completePercentage = 90 //Percentage of Complete Tasks //90
	val randomFeeder = Iterator.continually(Map("cancel-percentage" -> Random.nextInt(100)))
	val debugMode = System.getProperty("debug", "off")
	val feedPCSCWUserData = csv("PCSCWUserData.csv").circular

	val execute = {

		feed(feedPCSCWUserData)
		.exec(XuiHelper.Homepage)
		.exec(XuiHelper.Login("#{email}", "#{password}"))
		.exec(SearchCase.execute)
		.exec(_.set("taskName", "NewClaimCreateNewHearing"))
		.exec(ViewCase.execute)
		.feed(randomFeeder)
		.doIfOrElse(session => if (debugMode == "off") session("cancel-percentage").as[Int] < completePercentage else true) {
			exec(AssignTask.execute)
			.exec(CreateNewHearing.execute)
			.exec(_.remove("taskId"))
			.exec(_.set("taskName", "ReviewDateDue"))
			.exec(ViewCase.execute)
			.exec(AssignTask.execute)
			.exec(ReviewDateDue.execute)
		}
		{
			exec(CancelTask.execute)
		}
		.exec(XuiHelper.Logout)
	}

}
