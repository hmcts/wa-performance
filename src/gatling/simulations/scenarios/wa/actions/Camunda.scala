package scenarios.wa.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utilities.DateUtils
import utils.Environment

object Camunda {

  val authenticate = {

    exec(http("CCD_AuthLease")
      .post(Environment.rpeUrl + "/testing-support/lease")
      .body(StringBody("""{"microservice":"IdamWebApi"}""")).asJson
      .check(regex("(.+)").saveAs("IdamWebApiBearerToken")))

    .exec(http("CCD_AuthLease")
      .post(Environment.rpeUrl + "/testing-support/lease")
      .body(StringBody("""{"microservice":"wa_task_management_api"}""")).asJson
      .check(regex("(.+)").saveAs("wa_task_management_apiBearerToken")))
  }

  val PostCaseTaskAttributes =

    exec(authenticate)

    .exec(_.set("yesterdayDate", DateUtils.getDatePast("yyyy-MM-dd", days = 1)))

    .exec(http("PostCamundaTaskAttributes")
      .post(Environment.camundaURL + "/engine-rest/message")
      .header("ServiceAuthorization", "#{IdamWebApiBearerToken}")
      .header("Content-type", "application/json")
      .body(ElFileBody("waBodies/PostCaseTaskAttributes.json")))

    .pause(Environment.constantthinkTime)

    .exec(http("GetCaseTaskDetails")
      .get(Environment.camundaURL + "/engine-rest/task?processVariables=caseId_eq_#{caseId}")
      .header("ServiceAuthorization", "Bearer #{IdamWebApiBearerToken}")
      .header("Accept", "application/json")
      .check(jsonPath("$[-1].created").saveAs("taskCreated"))
      .check(jsonPath("$[-1].due").saveAs("taskDue"))
      .check(jsonPath("$[-1].id").saveAs("id")))

    .pause(Environment.constantthinkTime)

}