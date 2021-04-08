package uk.gov.hmcts.wa.scenarios.utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Environment {

  val minThinkTime = 10 //10
  val maxThinkTime = 30 //30
  val constantthinkTime = 1 //7

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
}
