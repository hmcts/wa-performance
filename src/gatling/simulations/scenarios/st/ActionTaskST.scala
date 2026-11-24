package scenarios.st

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.st.actions._
import xui.XuiHelper

object ActionTaskST {

  val feedSTUserData = csv("STUserData.csv").circular

  val execute = {

    feed(feedSTUserData)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{user}", "#{password}"))
    .exec(SearchCase.execute)
    .exec(_.set("taskName", "registerNewCase"))
    .exec(ViewCase.execute)
    .exec(AssignTask.execute)
    .exec(EditCase.execute)
    .pause(30)
    .exec(_.remove("taskId"))
    .exec(_.set("taskName", "vetNewCaseDocuments"))
    .exec(ViewCase.execute)
    .exec(AssignTask.execute)
    .exec(BuildCase.execute)
    .exec(XuiHelper.Logout)
  }
}