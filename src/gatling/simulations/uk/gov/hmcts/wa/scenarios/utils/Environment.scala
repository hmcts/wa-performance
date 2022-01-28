package uk.gov.hmcts.wa.scenarios.utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Environment {

  val minThinkTime = 10 //10
  val maxThinkTime = 30 //30
  val constantthinkTime = 7 //7

  val minWaitForNextIteration = 60 //120
  val maxWaitForNextIteration = 120 //240
  val HttpProtocol = http

  val idamURL = "https://idam-web-public.perftest.platform.hmcts.net"
  val idamAPI = "https://idam-api.perftest.platform.hmcts.net"
  val ccdEnvurl = "https://www-ccd.perftest.platform.hmcts.net"
  val ccdDataStoreUrl = "ccd-data-store-api-perftest.service.core-compute-perftest.internal"
  val baseURL = "https://gateway-ccd.perftest.platform.hmcts.net"
  val s2sUrl = "http://rpe-service-auth-provider-perftest.service.core-compute-perftest.internal"
  val xuiMCUrl = "https://manage-case.perftest.platform.hmcts.net/oauth2/callback"
  val xuiBaseURL = "https://manage-case.perftest.platform.hmcts.net"
  val dmStore = "http://dm-store-perftest.service.core-compute-perftest.internal"
  val waTMURL = "http://wa-task-management-api-perftest.service.core-compute-perftest.internal"
  val ccdRedirectUri = "https://ccd-data-store-api-perftest.service.core-compute-perftest.internal/oauth2redirect"
  val camundaURL = "http://camunda-api-perftest.service.core-compute-perftest.internal"
  val waWorkflowApiURL = "http://wa-workflow-api-perftest.service.core-compute-perftest.internal"

  val ClearSessionVariables =
    exec(flushHttpCache)
    .exec(flushCookieJar)
    .exec(_.remove("idamId"))
    .exec(_.remove("IACUserName"))
    .exec(_.remove("IACUserPassword"))
    .exec(_.remove("bearerToken"))
    .exec(_.remove("authCookie"))
    .exec(_.remove("code"))
    .exec(_.remove("access_token"))
    .exec(_.remove("eventToken"))
    .exec(_.remove("gatling.http.cookies"))
    .exec(_.remove("gatling.http.cache.contentCache"))
    .exec(_.remove("gatling.http.cache.redirects"))
    .exec(_.remove("gatling.http.ssl.sslContexts"))
}
