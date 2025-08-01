package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object Homepage {

  val BaseURL = Environment.xuiBaseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  /*====================================================================================
  *Manage Case Homepage
  *=====================================================================================*/

  val XUIHomePage =

    exec(flushHttpCache)
      .exec(flushCookieJar)

      .group("XUI_010_Homepage") {
        exec(http("XUI_010_005_Homepage")
          .get("/")
          .headers(Headers.navigationHeader)
          .header("sec-fetch-site", "none"))

          .exec(Common.configurationui)

          .exec(Common.configJson)

          .exec(Common.TsAndCs)

          .exec(Common.configUI)

          // .exec(Common.userDetails)
          .exec(http("XUI_Common_000_UserDetails")
            .get("/api/user/details?refreshRoleAssignments=undefined")
            .headers(Headers.commonHeader)
            .header("accept", "application/json, text/plain, */*")
            .check(status.in(200, 304, 401)))

          .exec(Common.isAuthenticated)

          .exec(http("XUI_010_010_AuthLogin")
            .get("/auth/login")
            .headers(Headers.navigationHeader)
            .check(CsrfCheck.save)
            .check(regex("/oauth2/callback&amp;state=(.*)&amp;nonce=").saveAs("state"))
            .check(regex("nonce=(.*)&amp;response_type").saveAs("nonce")))
      }

      //Nov 2023: required to capture the xui-webapp cookie and feed it back in after login. We were facing an issue whereby after the first login,
      //subsequent logins were generating a new xui-webapp cookie during the login (rather than using the existing one generated by auth/login),
      //and this was causing issues when making subsequent requests to get jurisdictions, work-basket-inputs, etc, throwing a 401.
      .exec(getCookieValue(CookieKey("xui-webapp").withDomain(BaseURL.replace("https://", "")).saveAs("xuiWebAppCookie")))

      .pause(MinThinkTime, MaxThinkTime)

}