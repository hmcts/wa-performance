package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiPrl {

  val baseURL = Environment.xuiBaseURL

  val SearchCase = 

    group("XUI_GlobalSearch_Request") {
      exec(Common.isAuthenticated)
      .exec(Common.apiUserDetails)

      .exec(http("XUI_GlobalSearch_010_Services")
        .get("/api/globalSearch/services")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_GlobalSearch_010_JurisdictionsRead")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .pause(Environment.constantthinkTime)

      .exec(http("XUI_GlobalSearch_020_Request")
        .post("/api/globalsearch/results")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/FPLCaseSearch.json")))

      .exec(Common.isAuthenticated)
      
      .exec(http("XUI_GlobalSearch_020_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

  val ViewCase = 

    exec(Common.isAuthenticated)
    .exec(Common.waSupportedJurisdictions)
    .exec(Common.apiUserDetails)

    .exec(http("XUI_ViewCase_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_PRL_SelectCaseTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(XUIHeaders.xuiMainHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{xsrfToken}")
      .check(jsonPath("$..[?(@.type=='checkApplicationFL401')].type").optional.saveAs("taskType"))
      .check(jsonPath("$..[?(@.type=='checkApplicationFL401')].id").optional.saveAs("taskId")))

    //Save taskType as nothing if it doesn't exist yet
    .doIf("#{taskType.isUndefined()}") {
      exec(_.set("taskType", ""))
    }

    .exec(_.set("counter", 0))

    // Loop until the task type matches "checkApplicationFL401"
    .asLongAs(session => session("taskType").as[String] != "checkApplicationFL401" && session("counter").as[Int] < 30, "counter") {
      exec(http("XUI_PRL_SelectCaseTaskRepeat")
        .get("/workallocation/case/task/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{xsrfToken}")
        // .check(jsonPath("$[0].id").optional.saveAs("taskId"))
        .check(jsonPath("$..[?(@.type=='checkApplicationFL401')].type").optional.saveAs("taskType"))
        .check(jsonPath("$..[?(@.type=='checkApplicationFL401')].id").optional.saveAs("taskId")))

      .pause(10) // Wait between retries

    //   // Log task Type
    //   .exec (session => {
    //     println(s"Current Task Type: ${session("taskType").as[String]}")
    //     session
    // })
    }

    .doIf(session => session("counter").as[Int] == 30) {
      // val newSession2 = session.markAsFailed

      exec (session => {
        println("No task was created, marking as failed")
        session
      })
    }

    .pause(Environment.constantthinkTime)

  val AddCaseNumber = 

    group("XUI_PRL_AddCaseNumber_Start") {
      exec(http("XUI_PRL_AddCaseNumber_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/fl401AddCaseNumber/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .exec(Common.isAuthenticated)
      .exec(Common.apiUserDetails)

      .exec(http("XUI_PRL_AddCaseNumber_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/fl401AddCaseNumber?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))
      
      .exec(Common.profile)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_PRL_AddCaseNumber_Page1") {
      exec(http("XUI_PRL_AddCaseNumber_Page1")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401AddCaseNumber1")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/PRLAddCaseNumberPage1.json")))

      .exec(http("XUI_PRL_AddCaseNumber_Page1GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_PRL_AddCaseNumber_Submit") {
      exec(http("XUI_PRL_AddCaseNumber_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/PRLAddCaseNumberSubmit.json")))

      .exec(http("XUI_PRL_AddCaseNumber_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}"))

      .exec(Common.waJurisdictions)
      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

  val SendToGatekeeper = 

    exec(http("XUI_PRL_SelectCaseTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(XUIHeaders.xuiMainHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{xsrfToken}")
      .check(jsonPath("$..[?(@.type=='sendToGateKeeperFL401')].type").optional.saveAs("taskType"))
      .check(jsonPath("$..[?(@.type=='sendToGateKeeperFL401')].id").optional.saveAs("taskId")))

    //Save taskType as nothing if it doesn't exist yet
    .doIf("#{taskType.isUndefined()}") {
      exec(_.set("taskType", ""))
    }

    .exec(_.set("counter", 0))

    // Loop until the task type matches "sendToGateKeeperFL401"
    .asLongAs(session => session("taskType").as[String] != "sendToGateKeeperFL401" && session("counter").as[Int] < 30, "counter") {
      exec(http("XUI_PRL_SelectCaseTaskRepeat")
        .get("/workallocation/case/task/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{xsrfToken}")
        .check(jsonPath("$..[?(@.type=='sendToGateKeeperFL401')].type").optional.saveAs("taskType"))
        .check(jsonPath("$..[?(@.type=='sendToGateKeeperFL401')].id").optional.saveAs("taskId")))

      .pause(5, 10) // Wait between retries

    //   // Log task Type
    //   .exec (session => {
    //     println(s"Current Task Type: ${session("taskType").as[String]}")
    //     session
    // })
    }

    .doIf(session => session("counter").as[Int] == 30) {
      // val newSession2 = session.markAsFailed

      exec (session => {
        println("No task was created, marking as failed")
        session
      })
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_PRL_SendToGatekeeper_Start"){
      exec(http("XUI_PRL_SendToGatekeeper_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/sendToGateKeeper?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(http("XUI_PRL_SendToGatekeeper_GetTask")
        .get("/cases/case-details/#{caseId}/trigger/sendToGateKeeper/sendToGateKeeper1?tid=#{taskId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.configUI)
      .exec(Common.TsAndCs)
      .exec(Common.apiUserDetails)

      .exec(http("XUI_PRL_SendToGatekeeper_GetTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/sendToGateKeeper/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/json")
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_PRL_SendToGatekeeper_Page1") {
      exec(http("XUI_PRL_SendToGatekeeper_Page1")
        .post("/data/case-types/PRLAPPS/validate?pageId=sendToGateKeeper1")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/PRLSendToGatekeeper1.json")))

      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_PRL_SendToGatekeeper_Submit") {
      exec(http("XUI_PRL_SendToGatekeeper_Submit")
        .post("/data/cases/#{caseId}/events")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/PRLSendToGatekeeperSubmit.json")))

      .exec(http("XUI_PRL_SendToGatekeeper_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}"))

      .exec(Common.waJurisdictions)
      .exec(Common.apiUserDetails)
    }
}