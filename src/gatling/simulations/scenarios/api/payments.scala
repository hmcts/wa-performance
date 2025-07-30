package scenarios.api

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.AzureKeyVault
import utils.Environment
import java.io.{BufferedWriter, FileWriter}

object payments {

  val idamAPIURL = "https://idam-api.#{env}.platform.hmcts.net"
  val rpeAPIURL = "http://rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"
  val clientSecret = AzureKeyVault.loadClientSecret("ccpay-perftest", "paybubble-idam-client-secret")
  val clientId = "paybubble"
  val microservice = "xui_webapp"
  val civilmicroservice = "civil_service"

  val authenticate = {

    exec(http("CCD_AuthLease")
      .post(rpeAPIURL + "/testing-support/lease")
      .body(StringBody(s"""{"microservice":"$microservice"}""")).asJson
      .check(regex("(.+)").saveAs("xui_webappBearerToken"))
    )

    .exec(http("CCD_AuthLease")
      .post(rpeAPIURL + "/testing-support/lease")
      .body(StringBody(s"""{"microservice":"$civilmicroservice"}""")).asJson
      .check(regex("(.+)").saveAs("civil_serviceBearerToken"))
    )

    .exec(http("CCD_GetBearerToken")
      .post(idamAPIURL + "/o/token")
      .formParam("grant_type", "password")
      .formParam("username", "#{email}")
      .formParam("password", "#{password}")
      .formParam("client_id", clientId)
      .formParam("client_secret", clientSecret)
      .formParam("scope", "openid profile roles search-user")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("access_tokenPayments"))
    )

    .exec(http("CCD_GetIdamID")
      .get(idamAPIURL + "/details")
      .header("Authorization", "Bearer #{bearerToken}")
      .check(jsonPath("$.id").saveAs("idamId")))
    }

  val AddCivilPayment =

    exec(authenticate)

    .exec(http("PaymentAPI_GetCasePaymentOrders")
      .get("http://payment-api-#{env}.service.core-compute-#{env}.internal/case-payment-orders?case_ids=#{caseId}")
      .header("Authorization", "Bearer #{access_tokenPayments}")
      .header("ServiceAuthorization", "#{xui_webappBearerToken}")
      .header("Content-Type","application/json")
      .header("accept","*/*")
      .check(jsonPath("$.content[0].orderReference").saveAs("caseIdPaymentRef")))

    .pause(Environment.constantthinkTime)

    .tryMax(2) {
      exec(http("API_Civil_AddPayment")
        .put("http://civil-service-#{env}.service.core-compute-#{env}.internal/service-request-update-claim-issued")
        .header("Authorization", "Bearer #{access_tokenPayments}")
        .header("ServiceAuthorization", "#{civil_serviceBearerToken}")
        .header("Content-type", "application/json")
        .body(ElFileBody("civilBodies/AddPayment.json")))
      }

    .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("CivilCreatedCaseIds.csv", true))
        try {
          fw.write(session("caseId").as[String] + "\r\n")
        }
        finally fw.close()
        session
    }

    .pause(Environment.constantthinkTime)
}