package scenarios.wa.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment

object TaskManagement {

  val authenticate =

    exec(http("CCD_AuthLease")
      .post(Environment.rpeUrl + "/testing-support/lease")
      .body(StringBody("""{"microservice":"wa_task_management_api"}""")).asJson
      .check(regex("(.+)").saveAs("wa_task_management_apiBearerToken")))

  val PostTask =

    doIf("#{id.exists()}") {
      exec(authenticate)

      .exec(http("PostTask_#{taskType}")
        .post(Environment.waTMURL + "/task/#{id}/initiation")
        .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
        .header("Accept", "application/json")
        .header("Content-type", "application/json")
        .body(ElFileBody("waBodies/PostTask.json"))
        .check(jsonPath("$.task_id").saveAs("taskId")))

      .pause(Environment.constantthinkTime)
    }

  val GetTask = {

    exec(http("WA_GetTask")
      .get(Environment.waTMURL + "/task/#{taskId}")
      .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("Content-Type", "application/json"))

    .pause(Environment.constantthinkTime)
  }

  val SearchTask = {

    //Retrieve a list of Task resources identified by set of search criteria

    repeat(10, "counter") {

      exec(_.set("order", "asc"))

      .exec(http("WA_SearchTask_AllWork_Page_Ascending")
        .post(Environment.waTMURL + "/task?first_result=#{counter}&max_results=25")
        .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
        .header("Authorization", "Bearer #{bearerToken}")
        .header("Content-Type", "application/json")
        .body(ElFileBody("WARequests/WA_SearchAllWork.json")))

      .pause(Environment.constantthinkTime)

      .exec(http("WA_SearchTask_AvailableTasks_Page_Ascending")
        .post(Environment.waTMURL + "/task?first_result=#{counter}&max_results=25")
        .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
        .header("Authorization", "Bearer #{bearerToken}")
        .header("Content-Type", "application/json")
        .body(ElFileBody("WARequests/WA_SearchAvailableTasks.json")))

      .pause(Environment.constantthinkTime)

      .exec(_.set("order", "desc"))

      .exec(http("WA_SearchTask_AllWork_Page_Descending")
        .post(Environment.waTMURL + "/task?first_result=#{counter}&max_results=25")
        .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
        .header("Authorization", "Bearer #{bearerToken}")
        .header("Content-Type", "application/json")
        .body(ElFileBody("WARequests/WA_SearchAllWork.json")))

      .pause(Environment.constantthinkTime)

      .exec(http("WA_SearchTask_AvailableTasks_Page_Descending")
        .post(Environment.waTMURL + "/task?first_result=#{counter}&max_results=25")
        .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
        .header("Authorization", "Bearer #{bearerToken}")
        .header("Content-Type", "application/json")
        .body(ElFileBody("WARequests/WA_SearchAvailableTasks.json")))

      .pause(Environment.constantthinkTime)

      .doIf("#{taskId.exists()}") {
        exec(http("WA_GetTask")
          .get(Environment.waTMURL + "/task/#{taskId}")
          .header("ServiceAuthorization", "Bearer #{wa_task_management_apiBearerToken}")
          .header("Authorization", "Bearer #{bearerToken}")
          .header("Content-Type", "application/json"))

        .pause(Environment.constantthinkTime)
      }
    }
  }


  /*,
    {
      "key": "location",
      "operator": "IN",
      "values": [
        "765324"
      ]
    }
    */
}