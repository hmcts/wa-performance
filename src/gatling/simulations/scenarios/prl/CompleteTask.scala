package scenarios.prl

import io.gatling.core.Predef._
import scenarios.common.wa._
import scenarios.common.xui._
import scenarios.prl.actions._
import xui.XuiHelper
object CompleteTask {

  val feedPRLTribunalUsers = csv("PRLTribunalUserData.csv").circular

  val execute = {

    feed(feedPRLTribunalUsers)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{user}", "#{password}"))

  }
}