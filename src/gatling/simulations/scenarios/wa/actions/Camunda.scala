package scenarios.wa.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Camunda {

  val rpeAPIURL = "http://rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"
  val camundaURL = "http://camunda-api-perftest.service.core-compute-perftest.internal"

  val authenticate = {

    exec(http("CCD_AuthLease")
      .post(rpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"IdamWebApi"}""")).asJson
      .check(regex("(.+)").saveAs("IdamWebApiBearerToken"))
    )

    .exec(http("CCD_AuthLease")
      .post(rpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"wa_task_management_api"}""")).asJson
      .check(regex("(.+)").saveAs("wa_task_management_apiBearerToken"))
    )
  }

  val PostCaseTaskAttributes =

    exec(authenticate)

    .exec(http("PostCamundaTaskAttributes")
      .post(camundaURL + "/engine-rest/message")
      .header("ServiceAuthorization", "#{IdamWebApiBearerToken}")
      .header("Content-type", "application/json")
      .body(ElFileBody("waBodies/PostCaseTaskAttributes.json"))
    )

    .pause(7)

    .exec(http("GetCaseTaskDetails")
      .get(camundaURL + "/engine-rest/task?processVariables=caseId_eq_#{caseId}")
      .header("ServiceAuthorization", "Bearer #{IdamWebApiBearerToken}")
      .header("Accept", "application/json")
      .check(jsonPath("$[0].created").saveAs("taskCreated"))
      .check(jsonPath("$[0].due").saveAs("taskDue"))
      .check(jsonPath("$[0].id").saveAs("id"))
    )

}
