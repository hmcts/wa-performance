package scenarios.civil.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef.http
import utils.Environment

object UpdateDate {

  val execute =

    exec(http("API_Civil_UpdateDate")
      .put(Environment.civilUrl + "/testing-support/case/#{caseId}")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("Content-type", "application/json")
      .body(ElFileBody("civilBodies/UpdateClaimDate.json")))

    .pause(Environment.constantthinkTime)

}