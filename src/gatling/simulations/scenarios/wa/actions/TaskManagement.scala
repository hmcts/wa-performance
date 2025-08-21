package scenarios.wa.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object TaskManagement {


  val rpeAPIURL = "http://rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"
  val taskManagementURL = "http://wa-task-management-api-#{env}.service.core-compute-#{env}.internal"

  val authenticate = {

    exec(http("CCD_AuthLease")
      .post(rpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"wa_task_management_api"}""")).asJson
      .check(regex("(.+)").saveAs("wa_task_management_apiBearerToken"))
    )
  }

  val PostTask =

    exec(authenticate)

    .exec(http("PostTask")
      .post(taskManagementURL + "/task/#{id}/initiation")
      .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
      .header("Accept", "application/json")
      .header("Content-type", "application/json")
      .body(ElFileBody("waBodies/PostTask.json"))
//      .check(bodyString.saveAs("BODY"))
    )

//    .exec(session => {
//      val response = session("BODY").as[String]
//      println(s"Response body: \n$response")
//      session
//    })
}