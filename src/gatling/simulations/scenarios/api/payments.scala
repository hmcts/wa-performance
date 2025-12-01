package scenarios.api

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.AzureKeyVault
import utils.Environment

object payments {

  val clientSecret = AzureKeyVault.loadClientSecret("ccpay-perftest", "paybubble-idam-client-secret")
  val clientId = "paybubble"
  val microservice = "xui_webapp"
  val civilmicroservice = "civil_service"

  val authenticate = {

    exec(http("CCD_AuthLease")
      .post(Environment.rpeUrl + "/testing-support/lease")
      .body(StringBody(s"""{"microservice":"$microservice"}""")).asJson
      .check(regex("(.+)").saveAs("xui_webappBearerToken"))
    )

    .exec(http("CCD_AuthLease")
      .post(Environment.rpeUrl + "/testing-support/lease")
      .body(StringBody(s"""{"microservice":"$civilmicroservice"}""")).asJson
      .check(regex("(.+)").saveAs("civil_serviceBearerToken"))
    )

    .exec(http("CCD_GetBearerToken")
      .post(Environment.idamAPI + "/o/token")
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
      .get(Environment.idamAPI + "/details")
      .header("Authorization", "Bearer #{bearerToken}")
      .check(jsonPath("$.id").saveAs("idamId")))
    }

  val AddCivilPayment =

    exec(authenticate)

    .exec(http("PaymentAPI_GetCasePaymentOrders")
      .get(Environment.paymentsUrl + "/case-payment-orders?case_ids=#{caseId}")
      .header("Authorization", "Bearer #{access_tokenPayments}")
      .header("ServiceAuthorization", "#{xui_webappBearerToken}")
      .header("Content-Type","application/json")
      .header("accept","*/*")
      .check(jsonPath("$.content[0].orderReference").saveAs("caseIdPaymentRef")))

    .pause(Environment.constantthinkTime)

    .tryMax(2) {
      exec(http("API_Civil_AddPayment")
        .put(Environment.civilUrl + "/service-request-update-claim-issued")
        .header("Authorization", "Bearer #{access_tokenPayments}")
        .header("ServiceAuthorization", "#{civil_serviceBearerToken}")
        .header("Content-type", "application/json")
        .body(ElFileBody("civilBodies/AddPayment.json")))
      }
      
    .pause(Environment.constantthinkTime)
}