package scenarios.fr

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.fr.actions._
import xui.XuiHelper
import ccd._

import scala.util.Random

object ActionTaskFRConsented {

	val feedFRUserData = csv("FRCWUserData.csv").circular
	val completePercentage = 90 //Percentage of Complete Tasks //90
	val randomFeeder = Iterator.continually(Map("cancel-percentage" -> Random.nextInt(100)))
	val debugMode = System.getProperty("debug", "off")

	val execute =

		feed(feedFRUserData)
		.exec(XuiHelper.Homepage)
		.exec(XuiHelper.Login("#{email}", "#{password}"))
		.exec(SearchCase.execute)
		.exec(_.set("taskName", "checkHelpWithFees"))
		.exec(ViewCase.execute)
		.feed(randomFeeder)
		.doIfOrElse(session => if (debugMode == "off") session("cancel-percentage").as[Int] < completePercentage else true) {
			exec(AssignTask.execute)
			.exec(HWFAcceptedIssue.execute)
		}
		{
			exec(CancelTask.execute)
		}
		.exec(_.remove("taskId"))
		.exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.DIVORCE_FinancialRemedyMVP2, "#{caseId}", "attachScannedDocs", "frBodies/consented/FRAttachScannedDocs.json"))
		.exec(_.set("taskName", "processScannedDocuments"))
		.exec(ViewCase.execute)
		.feed(randomFeeder)
		.doIfOrElse(session => if (debugMode == "off") session("cancel-percentage").as[Int] < completePercentage else true) {
			exec(AssignTask.execute)
			.exec(AttachScannedDocument.execute)
		}
		{
			exec(CancelTask.execute)
		}
		.exec(XuiHelper.Logout)

}
