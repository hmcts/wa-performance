package scenarios.common.xui

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui._

object SearchCase {

  val execute =

    group("XUI_GlobalSearch_Request") {
      exec(Common.isAuthenticated)
      .exec(Common.apiUserDetails)

      .exec(http("XUI_GlobalSearch_010_Services")
        .get("/api/globalSearch/services")
        .headers(Headers.commonHeader))

      .exec(http("XUI_GlobalSearch_010_JurisdictionsRead")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .pause(Environment.constantthinkTime)

      .exec(http("XUI_GlobalSearch_020_Request")
        .post("/api/globalsearch/results")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("xuiBodies/FPLCaseSearch.json")))

      .exec(Common.isAuthenticated)

      .exec(http("XUI_GlobalSearch_020_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader).header("x-xsrf-token", "#{XSRFToken}")
        .header("content-type", "application/json")
        .header("experimental", "true")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

}