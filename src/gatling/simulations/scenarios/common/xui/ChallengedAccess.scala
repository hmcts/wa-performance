package scenarios.common.xui

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.DateUtils
import utils._

object ChallengedAccess {

  val JudicialChallengedAccess =

		exec(_.set("currentDate", DateUtils.getDateNow("yyyy-MM-dd")))

    .exec(http("XUI_RequestChallengedAccess_Request")
			.post("/api/challenged-access-request")
			.headers(Headers.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{XSRFToken}")
			.body(ElFileBody("xuiBodies/JudicialChallengedAccessRequest.json")))

    .exec(http("XUI_RequestChallengedAccess_UserDetails")
			.get("/api/user/details")
			.headers(Headers.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

}