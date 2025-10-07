package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Environment {

  val constantthinkTime = 10 //7

  val HttpProtocol = http

  val idamURL = "https://idam-web-public.#{env}.platform.hmcts.net"
  val idamAPI = "https://idam-api.#{env}.platform.hmcts.net"
  val rpeUrl = "http://rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"
  val xuiBaseURL = "https://manage-case.#{env}.platform.hmcts.net"
  val waTMURL = "http://wa-task-management-api-#{env}.service.core-compute-#{env}.internal"
  val camundaURL = "http://camunda-api-#{env}.service.core-compute-#{env}.internal"
  val cuiStURL = "https://sptribs-frontend.#{env}.platform.hmcts.net"
  val paymentsUrl = "http://payment-api-#{env}.service.core-compute-#{env}.internal"
  val civilUrl = "http://civil-service-#{env}.service.core-compute-#{env}.internal"

}