package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._
import java.io.{BufferedWriter, FileWriter}

object wataskmanagement {

val config: Config = ConfigFactory.load()
val waUrl = Environment.waTMURL
val s2sUrl = Environment.s2sUrl
val IdamAPI = Environment.idamAPI
val CamundaUrl = Environment.camundaURL
val ccdRedirectUri = Environment.ccdRedirectUri
val ccdClientId = "ccd_gateway"
val ccdScope = "openid profile authorities acr roles openid profile roles"
val ccdGatewayClientSecret = config.getString("ccdGatewayCS")
val taskListFeeder = csv("WA_TaskList.csv").circular
val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
val feedIACUserData = csv("IACUserData.csv").circular
val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular
val caseListFeeder = csv("WA_CaseList.csv").circular
val feedStaticTasksFeeder = csv("WA_StaticTasks.csv").random

val WAS2SLogin = 

  exec(http("GetS2SToken")
    .post(s2sUrl + "/testing-support/lease")
    .header("Content-Type", "application/json")
    .body(StringBody("{\"microservice\":\"wa_task_management_api\"}")) //wa_task_management_api
    .check(bodyString.saveAs("bearerToken")))
    .exitHereIfFailed

val WASeniorIdamLogin =
  
  feed(feedWASeniorUserData)

  .exec(http("OIDC01_Authenticate")
    .post(IdamAPI + "/authenticate")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .formParam("username", "${email}")
    .formParam("password", "${password}")
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
    .check(jsonPath("$.access_token").saveAs("access_token")))
    .exitHereIfFailed

val WATribunalIdamLogin =
  
  feed(feedWATribunalUserData)

  .exec(http("OIDC01_Authenticate")
    .post(IdamAPI + "/authenticate")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .formParam("username", "${email}")
    .formParam("password", "${password}")
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
    .check(jsonPath("$.access_token").saveAs("access_token")))
    .exitHereIfFailed

val GetTask =

  //Retrieve a Task Resource identified by its unique id.

  feed(taskListFeeder)

  .exec(http("WA_GetTask")
    .get(waUrl + "/task/${taskId}")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val GetTaskForSearches =

  //Retrieve a Task Resource identified by its unique id.

  feed(feedStaticTasksFeeder)

  .exec(http("WA_SearchGetTask")
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

  feed(taskCancelListFeeder)

  .exec(http("WA_CancelTask")
    .post(waUrl + "/task/${taskId}/cancel") //${taskId}
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val ClaimTask =

  //Claim the identified Task for the currently logged in user.

  feed(taskListFeeder)

  .exec(http("WA_ClaimTask")
    .post(waUrl + "/task/${taskId}/claim")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val CompleteTask =

  //Completes a Task identified by an id.

  exec(http("WA_CompleteTask")
    .post(waUrl + "/task/${taskId}/complete") //${taskId}
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val UnclaimTask =

  //Unclaim the identified Task for the currently logged in user.

  feed(taskListFeeder)

  .exec(http("WA_UnclaimTask")
    .post(waUrl + "/task/${taskId}/unclaim")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .header("Authorization", "Bearer ${access_token}")
    .header("Content-Type", "application/json"))

val CamundaGetCase =

  feed(caseListFeeder)

  .exec(http("Camunda_GetTask")
    .get(CamundaUrl + "/engine-rest/task?processVariables=caseId_eq_${caseId}")
    .header("ServiceAuthorization", "Bearer ${bearerToken}")
    .check(regex("""id":"(.*)","name""").saveAs("taskId")))

  // .doIf(session=>session("statusvalue").as[String].contains("200")) {
    .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("TaskIDs.csv", true))
        try {
          fw.write(session("caseId").as[String] + ","+session("taskId").as[String] + "\r\n")
        }
        finally fw.close()
        session
    }
  // }
  

}