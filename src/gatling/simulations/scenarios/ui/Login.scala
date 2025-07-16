package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object Login {

  val BaseURL = Environment.xuiBaseURL
  val IdamUrl = Environment.idamURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  /*====================================================================================
  *Manage Case Login
  *=====================================================================================*/

  val XUILogin =

    group("XUI_020_Login") {
      exec(http("XUI_020_005_Login")
        .post(IdamUrl + "/login?client_id=xuiwebapp&redirect_uri=" + BaseURL + "/oauth2/callback&state=#{state}&nonce=#{nonce}&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user%20search-user&prompt=")
        .formParam("username", "#{user}")
        .formParam("password", "#{password}")
        .formParam("azureLoginEnabled", "true")
        .formParam("mojLoginEnabled", "true")
        .formParam("selfRegistrationEnabled", "false")
        .formParam("_csrf", "#{csrf}")
        .headers(Headers.navigationHeader)
        .headers(Headers.postHeader)
        .check(regex("Manage cases")))

      //see xui-webapp cookie capture in the Homepage scenario for details of why this is being used
      .exec(addCookie(Cookie("xui-webapp", "#{xuiWebAppCookie}")
        .withMaxAge(28800)
        .withSecure(true)))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      //if there is no in-flight case, set the case to 0 for the activity calls
      .doIf("#{caseId.isUndefined()}") {
        exec(_.set("caseId", "0"))
      }

      // .exec(Common.caseActivityGet)

      .exec(http("XUI_020_010_Jurisdictions")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("id")))

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))
      .exec(getCookieValue(CookieKey("__auth__").saveAs("bearerToken")))

      .exec(Common.orgDetails)

      .exec(http("XUI_020_015_WorkBasketInputs")
        .get("/data/internal/case-types/#{caseType}/work-basket-inputs")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-workbasket-input-details.v2+json;charset=UTF-8")
        .check(regex("workbasketInputs|Not Found"))
        .check(status.in(200, 404)))

      .exec(http("XUI_020_020_SearchCases")
        .post("/data/internal/searchCases?ctid=#{caseType}&use_case=WORKBASKET&view=WORKBASKET&page=1")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .formParam("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"size":25}"""))
        .check(substring("columns")))

    }
      .pause(MinThinkTime , MaxThinkTime)
}