package scenarios.api

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.AzureKeyVault
import utils.Environment
import ccd._

object payments {

  val clientSecret = AzureKeyVault.loadClientSecret("ccpay-perftest", "paybubble-idam-client-secret", "PAYBUBBLE_CLIENT_SECRET")
  val clientId = "paybubble"
  val microservice = "xui_webapp"
  val civilmicroservice = "civil_service"

  val AddCivilPayment =

    exec(CcdHelper.authenticate("#{email}", "#{password}", microservice, clientId))

    .exec(http("CCD_AuthLease")
      .post(Environment.rpeUrl + "/testing-support/lease")
      .body(StringBody(s"""{"microservice":"$civilmicroservice"}""")).asJson
      .check(regex("(.+)").saveAs("civil_serviceBearerToken"))
    )

    .exec(http("PaymentAPI_GetCasePaymentOrders")
      .get(Environment.paymentsUrl + "/case-payment-orders?case_ids=#{caseId}")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type","application/json")
      .header("accept","*/*")
      .check(jsonPath("$.content[0].orderReference").saveAs("caseIdPaymentRef")))

    .pause(Environment.constantthinkTime)

    .tryMax(2) {
      exec(http("API_Civil_AddPayment")
        .put(Environment.civilUrl + "/service-request-update-claim-issued")
        .header("Authorization", "Bearer #{bearerToken}")
        .header("ServiceAuthorization", "#{civil_serviceBearerToken}")
        .header("Content-type", "application/json")
        .body(ElFileBody("civilBodies/AddPayment.json")))
      }
      
    .pause(Environment.constantthinkTime)
}