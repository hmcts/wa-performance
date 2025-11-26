package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

import java.io.{BufferedWriter, FileWriter}

object wataskmanagement {

  val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular

  val GetAllTasks =

    exec(http("WA_GetAllTasks")
      .post(Environment.waTMURL + "/task") //?first_result=1&max_results=1000")
      .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type", "application/json")
      .body(ElFileBody("WARequests/WA_GetAllTasksNew.json"))
      .check(bodyString.saveAs("Response")))

  val CancelTask =

    //Cancel a Task identified by an id.

    exec(http("WA_CancelTask")
      .post(Environment.waTMURL + "/task/#{taskId}/cancel")
      .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type", "application/json"))

    .pause(Environment.constantthinkTime)

  val GetTask =

    //Retrieve a Task Resource identified by its unique id.

    // feed(taskListFeeder)

    exec(http("WA_GetTask")
      .get(Environment.waTMURL + "/task/#{taskId}")
      .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
      .header("Authorization", "Bearer #{access_token}")
      .header("Content-Type", "application/json"))

    .pause(Environment.constantthinkTime)

  val SearchTask =

    //Retrieve a list of Task resources identified by set of search criteria

    exec(http("WA_SearchTask_First25")
      .post(Environment.waTMURL + "/task?first_result=0&max_results=25")
      .header("ServiceAuthorization", "Bearer ${wa_task_management_apiBearerToken}")
      .header("Authorization", "Bearer ${access_token}")
      .header("Content-Type", "application/json")
      .body(ElFileBody("WARequests/WA_Search1.json")))

  val CamundaGetCase =

    feed(taskCancelListFeeder)

    .exec(http("Camunda_GetTask")
      .get(Environment.camundaURL + "/engine-rest/task?processVariables=caseId_eq_#{caseId}") //#{caseId}
      .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
      .check(jsonPath("$[0].id").saveAs("taskId")))

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