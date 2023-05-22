package scenarios

import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID.randomUUID

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.session._
import io.gatling.commons.validation._
import utils._
import java.io.{BufferedWriter, FileWriter}

object wataskmanagement {

val config: Config = ConfigFactory.load()
val waUrl = Environment.waTMURL
val CamundaUrl = Environment.camundaURL
val ccdRedirectUri = Environment.ccdRedirectUri
val waWorkflowUrl = Environment.waWorkflowApiURL
val ccdClientId = "ccd_gateway"
val ccdScope = "openid profile authorities acr roles openid profile roles"
val ccdGatewayClientSecret = config.getString("auth.clientSecret")
val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
val feedIACUserData = csv("IACUserData.csv").circular
val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular
def randomkey: String = randomUUID.toString

val WAS2SLogin = 

  exec(http("WA_GetS2SToken")
    .post(Environment.s2sUrl + "/testing-support/lease")
    .header("Content-Type", "application/json")
    .body(StringBody("{\"microservice\":\"wa_task_management_api\"}")) 
    .check(bodyString.saveAs("bearerToken2")))
    .exitHereIfFailed

val WATaskS2SLogin = 

  exec(http("WA_GetS2SToken")
    .post(Environment.s2sUrl + "/testing-support/lease")
    .header("Content-Type", "application/json")
    .body(StringBody("{\"microservice\":\"wa_case_event_handler\"}")) 
    .check(bodyString.saveAs("bearerToken3")))
    .exitHereIfFailed

val WASeniorIdamLogin =
  
  feed(feedWASeniorUserData)

  .exec(http("WA_OIDC01_Authenticate")
    .post(Environment.idamAPI + "/authenticate")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .formParam("username", "#{email}")
    .formParam("password", "#{password}")
    .formParam("redirectUri", ccdRedirectUri)
    .formParam("originIp", "0:0:0:0:0:0:0:1")
    .check(status is 200)
    .check(headerRegex("Set-Cookie", "Idam.Session=(.*)").saveAs("authCookie")))
    .exitHereIfFailed

  .exec(http("WA_OIDC02_Authorize")
    .post(Environment.idamAPI + "/o/authorize?response_type=code&client_id=" + ccdClientId + "&redirect_uri=" + ccdRedirectUri + "&scope=" + ccdScope).disableFollowRedirect
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Cookie", "Idam.Session=#{authCookie}")
    .header("Content-Length", "0")
    .check(status is 302)
    .check(headerRegex("Location", "code=(.*)&client_id").saveAs("code")))
    .exitHereIfFailed

  .exec(http("WA_OIDC03_Token")
    .post(Environment.idamAPI + "/o/token?grant_type=authorization_code&code=#{code}&client_id=" + ccdClientId +"&redirect_uri=" + ccdRedirectUri + "&client_secret=" + ccdGatewayClientSecret)
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Content-Length", "0")
    .check(status is 200)
    .check(jsonPath("$.access_token").saveAs("access_token2")))
    .exitHereIfFailed

val WATribunalIdamLogin =
  
  feed(feedWATribunalUserData)

  .exec(http("OIDC01_Authenticate")
    .post(Environment.idamAPI + "/authenticate")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .formParam("username", "#{waemail}")
    .formParam("password", "#{wapassword}")
    .formParam("redirectUri", ccdRedirectUri)
    .formParam("originIp", "0:0:0:0:0:0:0:1")
    .check(status is 200)
    .check(headerRegex("Set-Cookie", "Idam.Session=(.*)").saveAs("authCookie")))
    .exitHereIfFailed

  .exec(http("OIDC02_Authorize")
    .post(Environment.idamAPI + "/o/authorize?response_type=code&client_id=" + ccdClientId + "&redirect_uri=" + ccdRedirectUri + "&scope=" + ccdScope).disableFollowRedirect
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Cookie", "Idam.Session=#{authCookie}")
    .header("Content-Length", "0")
    .check(status is 302)
    .check(headerRegex("Location", "code=(.*)&client_id").saveAs("code")))
    .exitHereIfFailed

  .exec(http("OIDC03_Token")
    .post(Environment.idamAPI + "/o/token?grant_type=authorization_code&code=#{code}&client_id=" + ccdClientId +"&redirect_uri=" + ccdRedirectUri + "&client_secret=" + ccdGatewayClientSecret)
    .header("Content-Type", "application/x-www-form-urlencoded")
    .header("Content-Length", "0")
    .check(status is 200)
    .check(jsonPath("$.access_token2").saveAs("access_token2")))
    .exitHereIfFailed

  .pause(Environment.constantthinkTime)

val GetAllTasks =

  exec(http("WA_GetAllTasks")
    .post(waUrl + "/task") //?first_result=1&max_results=1000")
    .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
    .header("Authorization", "Bearer #{access_token}")
    .header("Content-Type", "application/json")
    .body(ElFileBody("WARequests/WA_GetAllTasksNew.json"))
    // .check(jsonPath("$.tasks[0].id").saveAs("taskId"))
    .check(bodyString.saveAs("Response"))
    )

  .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("1000Tasks.json", true))
        try {
          fw.write(session("Response").as[String] + "\r\n")
        }
        finally fw.close()
        session
    } 


val GetTask =

  //Retrieve a Task Resource identified by its unique id.

  // feed(taskListFeeder)

  exec(http("WA_GetTask")
    .get(waUrl + "/task/f23dd3bd-9a4d-11ec-80f8-c656fc890203") //#{taskId}
    .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
    .header("Authorization", "Bearer #{access_token}")
    .header("Content-Type", "application/json"))

  .pause(Environment.constantthinkTime)

val CamundaGetCase =

  feed(taskCancelListFeeder)

  .exec(http("Camunda_GetTask")
    .get(CamundaUrl + "/engine-rest/task?processVariables=caseId_eq_#{caseId}") //#{caseId}
    .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
    .check(jsonPath("$[0].id").saveAs("taskId")))
    // .exitHereIfFailed

    // .pause(Environment.constantthinkTime)

    .exec {
      session =>
        val fw = new BufferedWriter(new FileWriter("CancelTaskIDs.csv", true))
        try {
          fw.write(session("caseId").as[String] + "," +session("taskId").as[String] + "\r\n")
        }
        finally fw.close()
        session
    }  

}