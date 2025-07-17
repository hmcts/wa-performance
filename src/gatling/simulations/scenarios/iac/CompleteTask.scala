package scenarios.iac

import io.gatling.core.Predef._
import scenarios.common.xui._
import scenarios.common.wa.AssignTask
import actions._
import xui.XuiHelper

object CompleteTask {

  val feedTribunalUserData = csv("WA_TribunalUsers.csv").circular

  val execute = {

    feed(feedTribunalUserData)
      .exec(XuiHelper.Homepage)
      .exec(XuiHelper.Login("#{email}", "#{password}"))
      .exec(SearchCase.execute)
      .exec(ViewCase.execute)
      .exec(AssignTask.execute)
      .exec(RequestRespondentEvidence.execute)
      .exec(XuiHelper.Logout)
  }

}