package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID.randomUUID

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.session._
import io.gatling.commons.validation._
import uk.gov.hmcts.wa.scenarios.utils._
import java.io.{BufferedWriter, FileWriter}

object wataskmanagement {

val config: Config = ConfigFactory.load()
val waUrl = Environment.waTMURL
val s2sUrl = Environment.s2sUrl
val IdamAPI = Environment.idamAPI
val CamundaUrl = Environment.camundaURL
val ccdRedirectUri = Environment.ccdRedirectUri
val waWorkflowUrl = Environment.waWorkflowApiURL
val ccdClientId = "ccd_gateway"
val ccdScope = "openid profile authorities acr roles openid profile roles"
val ccdGatewayClientSecret = config.getString("ccdGatewayCS")
val taskListFeeder = csv("WA_TaskList.csv").circular
val feedCompleteTaskListFeeder = csv("WA_TasksToComplete.csv")
val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
val feedIACUserData = csv("IACUserData.csv").circular
val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular
val caseListFeeder = csv("WA_CaseList.csv").circular
val feedStaticTasksFeeder = csv("WA_StaticTasks.csv").random
val pipelineTribunalFeeder = csv("WA_PipelineSenior.csv")
def randomkey: String = randomUUID.toString

val WAS2SLogin = 

  exec(http("WA_GetS2SToken")
    .post(s2sUrl + "/testing-support/lease")
    .header("Content-Type", "application/json")
    .body(StringBody("{\"microservice\":\"wa_task_management_api\"}")) 
    .check(bodyString.saveAs("bearerToken2")))
    .exitHereIfFailed

val WATaskS2SLogin = 

  exec(http("WA_GetS2SToken")
    .post(s2sUrl + "/testing-support/lease")
    .header("Content-Type", "application/json")
    .body(StringBody("{\"microservice\":\"wa_case_event_handler\"}")) 
    .check(bodyString.saveAs("bearerToken3")))
    .exitHereIfFailed

val WASeniorIdamLogin =
  
  feed(pipelineTribunalFeeder)

  .exec(http("WA_OIDC01_Authenticate")
    .post(IdamAPI + "/authenticate")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .formParam("username", "${waemail}")
    .formParam("password", "${wapassword}")
    .formParam("redirectUri", ccdRedirectUri)
    .formParam("originIp", "0:0:0:0:0:0:0:1")
    .check(status is 200)
    .check(headerRegex("Set-Cookie", "Idam.Session=(.*)").saveAs("authCookie")))
    .exitHereIfFailed

  .exec(http("WA_OIDC02_Authorize")
    .post(IdamAPI + "/o/authorize?response_type=code&client_id=" + ccdClientId + "&redirect_uri=" + ccdRedirectUri + "&scope=" + ccdScope).disableFollowRedirect
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Cookie", "Idam.Session=${authCookie}")
    .header("Content-Length", "0")
    .check(status is 302)
    .check(headerRegex("Location", "code=(.*)&client_id").saveAs("code")))
    .exitHereIfFailed

  .exec(http("WA_OIDC03_Token")
    .post(IdamAPI + "/o/token?grant_type=authorization_code&code=${code}&client_id=" + ccdClientId +"&redirect_uri=" + ccdRedirectUri + "&client_secret=" + ccdGatewayClientSecret)
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Content-Length", "0")
    .check(status is 200)
    .check(jsonPath("$.access_token").saveAs("access_token2")))
    .exitHereIfFailed

val WATribunalIdamLogin =
  
  feed(feedWATribunalUserData)

  .exec(http("OIDC01_Authenticate")
    .post(IdamAPI + "/authenticate")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .formParam("username", "${waemail}")
    .formParam("password", "${wapassword}")
    .formParam("redirectUri", ccdRedirectUri)
    .formParam("originIp", "0:0:0:0:0:0:0:1")
    .check(status is 200)
    .check(headerRegex("Set-Cookie", "Idam.Session=(.*)").saveAs("authCookie")))
    .exitHereIfFailed

  .exec(http("OIDC02_Authorize")
    .post(IdamAPI + "/o/authorize?response_type=code&client_id=" + ccdClientId + "&redirect_uri=" + ccdRedirectUri + "&scope=" + ccdScope).disableFollowRedirect
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Cookie", "Idam.Session=${authCookie}")
    .header("Content-Length", "0")
    .check(status is 302)
    .check(headerRegex("Location", "code=(.*)&client_id").saveAs("code")))
    .exitHereIfFailed

  .exec(http("OIDC03_Token")
    .post(IdamAPI + "/o/token?grant_type=authorization_code&code=${code}&client_id=" + ccdClientId +"&redirect_uri=" + ccdRedirectUri + "&client_secret=" + ccdGatewayClientSecret)
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Content-Length", "0")
    .check(status is 200)
    .check(jsonPath("$.access_token2").saveAs("access_token2")))
    .exitHereIfFailed

  .pause(Environment.constantthinkTime)

val CreateTask = 

  exec(_.setAll(
            ("createrandomkey", randomkey),
        ))

  .exec(http("WA_CreateTask")
    .post(waWorkflowUrl + "/workflow/message")
    .header("Content-Type", "application/json")
    .header("ServiceAuthorization", "Bearer ${bearerToken3}")
    .body(ElFileBody("WA_CreateTask.json")))
    // .exitHereIfFailed

  .pause(Environment.constantthinkTime)

  // // .doIf(session => !session.contains("taskId")) {
  // .doIf("${taskId.isUndefined()}") {
  //   // exec {
  //   //   session => 
  //   //     println(session("Task was not found, now exiting..."))
  //   // }
  //   exitHereIfFailed
  // }


val GetTask =

  //Retrieve a Task Resource identified by its unique id.

  // feed(taskListFeeder)

  exec(http("WA_GetTask")
    .get(waUrl + "/task/${taskId}") //${taskId}
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json"))

  .pause(Environment.constantthinkTime)

val GetTaskForCompletion =

  //Retrieve a Task Resource identified by its unique id.

  // feed(feedCompleteTaskListFeeder)

  exec(http("WA_GetTask")
    .get(waUrl + "/task/${taskId}")
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json"))
  
  .pause(Environment.constantthinkTime)

val GetTaskForSearches =

  //Retrieve a Task Resource identified by its unique id.

  feed(feedStaticTasksFeeder)

  .exec(http("WA_SearchGetTask")
    .get(waUrl + "/task/${taskId}")
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json"))

  .pause(Environment.constantthinkTime)

val PostTaskRetrieve = 

  //Retrieve a list of Task resources identified by set of search criteria.

  exec(http("WA_PostTaskRetrieve")
    .post(waUrl + "/task")
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json")
    .body(ElFileBody("WA_searchTaskRequest.json")))

  .pause(Environment.constantthinkTime)

val PostTaskSearchCompletable =

  //Retrieve a list of Task resources identified by set of search criteria that are eligible for automatic completion

  exec(http("WA_PostTaskSearchCompletable")
    .post(waUrl + "/task/search-for-completable")
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json")
    .body(ElFileBody("WA_searchTaskCompletable.json")))

  .pause(Environment.constantthinkTime)

val PostAssignTask =

  //Assign the identified Task to a specified user.

  exec(http("WA_PostAssignTask")
    .post(waUrl + "/task/${taskId}/assign")
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json")
    .body(ElFileBody("WA_assignTaskToUser.json")))

  .pause(Environment.constantthinkTime)

val CancelTask =

  //Cancel a Task identified by an id.

  feed(taskCancelListFeeder)

  .exec(http("WA_CancelTask")
    .post(waUrl + "/task/${taskId}/cancel") //${taskId}
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json"))

  .pause(Environment.constantthinkTime)

val ClaimTask =

  //Claim the identified Task for the currently logged in user.

  feed(taskListFeeder)

  .exec(http("WA_ClaimTask")
    .post(waUrl + "/task/${taskId}/claim")
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json"))

  .pause(Environment.constantthinkTime)

val CompleteTask =

  //Completes a Task identified by an id.

  exec(http("WA_CompleteTask")
    .post(waUrl + "/task/${taskId}/complete") //${taskId}
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json"))

  .pause(Environment.constantthinkTime)

val UnclaimTask =

  //Unclaim the identified Task for the currently logged in user.

  feed(taskListFeeder)

  .exec(http("WA_UnclaimTask")
    .post(waUrl + "/task/${taskId}/unclaim")
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .header("Authorization", "Bearer ${access_token2}")
    .header("Content-Type", "application/json"))

  .pause(Environment.constantthinkTime)

val CamundaGetCase =

  // feed(caseListFeeder)

  exec(http("Camunda_GetTask")
    .get(CamundaUrl + "/engine-rest/task?processVariables=caseId_eq_${caseId}") //${caseId}
    .header("ServiceAuthorization", "Bearer ${bearerToken2}")
    .check(regex("""id":"(.*?)","name":"Review""").saveAs("taskId")))
    .exitHereIfFailed

    .pause(Environment.constantthinkTime)

    // .exec {
    //   session =>
    //     val fw = new BufferedWriter(new FileWriter("TaskIDs.csv", true))
    //     try {
    //       fw.write(session("caseId").as[String] + ","+session("taskId").as[String] + "\r\n")
    //     }
    //     finally fw.close()
    //     session
    // }  

}