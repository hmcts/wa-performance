package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._

object wataskmanagement {

val config: Config = ConfigFactory.load()
val waUrl = Environment.waTMURL
val s2sUrl = Environment.s2sUrl
val taskListFeeder = csv("WA_TaskList.csv").circular
val feedIACUserData = csv("IACUserData.csv").circular

val WAAuthoriseUser = 

  feed(feedIACUserData)

  .exec(http("GetS2SToken")
    .post(s2sUrl + "/testing-support/lease")
    .header("Content-Type", "application/json")
    .body(StringBody("{\"microservice\":\"ccd_data\"}"))
    .check(bodyString.saveAs("bearerToken")))
    .exitHereIfFailed

val GetTask =

  feed(taskListFeeder)

  .exec(http("WA_GetTask")
    .get(waUrl + "/task/${taskId}")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val PostTaskRetrieve = 

  exec(http("WA_PostTaskRetrieve")
    .post(waUrl + "/task")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json")
    .body(ElFileBody("WA_searchTaskRequest.json")))
}