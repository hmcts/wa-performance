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
    .body(StringBody("{\"microservice\":\"ccd_data\"}")) //wa_task_management_api
    .check(bodyString.saveAs("bearerToken")))
    .exitHereIfFailed

val GetTask =

  //Retrieve a Task Resource identified by its unique id.

  feed(taskListFeeder)

  .exec(http("WA_GetTask")
    .get(waUrl + "/task/${taskId}")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val PostTaskRetrieve = 

  //Retrieve a list of Task resources identified by set of search criteria.

  exec(http("WA_PostTaskRetrieve")
    .post(waUrl + "/task")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json")
    .body(ElFileBody("WA_searchTaskRequest.json")))

val PostTaskSearchCompletable =

  //Retrieve a list of Task resources identified by set of search criteria that are eligible for automatic completion

  exec(http("WA_PostTaskSearchCompletable")
    .post(waUrl + "/task/search-for-completable")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json")
    .body(ElFileBody("WA_searchTaskCompletable.json")))

val PostAssignTask =

  //Assign the identified Task to a specified user.

  exec(http("WA_PostAssignTask")
    .post(waUrl + "/task/${taskId}/assign")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json")
    .body(ElFileBody("WA_assignTaskToUser.json")))

val CancelTask =

  //Cancel a Task identified by an id.

  exec(http("WA_CancelTask")
    .post(waUrl + "/task/${taskId}/cancel")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val ClaimTask =

  //Claim the identified Task for the currently logged in user.

  exec(http("WA_ClaimTask")
    .post(waUrl + "/task/${taskId}/claim")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val CompleteTask =

  //Completes a Task identified by an id.

  exec(http("WA_CompleteTask")
    .post(waUrl + "/task/${taskId}/complete")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val UnclaimTask =

  //Unclaim the identified Task for the currently logged in user.

  exec(http("WA_UnclaimTask")
    .post(waUrl + "/task/${taskId}/unclaim")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))
}