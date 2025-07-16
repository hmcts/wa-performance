package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment}
import xui._
import xuiIac._

object xuiIac {

  val baseURL = Environment.xuiBaseURL
  val feedTribunalUserData = csv("WA_TribunalUsers.csv").circular

  val CompleteIACTask = {

    exec(_.set("caseType", "Asylum"))
    .feed(feedTribunalUserData)
    .exec(XuiHelper.Homepage)
    .exec(XuiHelper.Login("#{email}", "#{password}"))
    .exec(xuiIac.SearchCase)
    .exec(xuiIac.ViewCase)
    .exec(xuiwa.AssignTask)
    .exec(xuiIac.RequestRespondentEvidence)
    .exec(XuiHelper.Logout)
  }

  val SearchCase = 

    group("XUI_GlobalSearch_Request") {
      exec(Common.isAuthenticated)
      .exec(Common.apiUserDetails)

      .exec(http("XUI_GlobalSearch_010_Services")
        .get("/api/globalSearch/services")
        .headers(Headers.commonHeader))

      .exec(http("XUI_GlobalSearch_010_JurisdictionsRead")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json"))

      .pause(Environment.constantthinkTime)

      .exec(http("XUI_GlobalSearch_020_Request")
        .post("/api/globalsearch/results")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/FPLCaseSearch.json")))

      .exec(Common.isAuthenticated)
      
      .exec(http("XUI_GlobalSearch_020_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
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
			.headers(Headers.commonHeader)
      .header("content-type", "application/json")
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_IAC_SelectCaseTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(Headers.commonHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{xsrfToken}")
      .check(jsonPath("$[0].id").optional.saveAs("taskId"))
      .check(jsonPath("$[0].type").optional.saveAs("taskType")))

    //Save taskType from response
    .exec(session => {
      // Initialise task type in session if it's not already present, ensure the variable exists before entering Loop
      session("taskType").asOption[String] match {
        case Some(taskType) => session
        case None => session.set("taskType", "")
      }
    })

    // Loop until the task type matches "reviewTheAppeal"
    .asLongAs(session => session("taskType").as[String] != "reviewTheAppeal") {
      exec(http("XUI_IAC_SelectCaseTaskRepeat")
        .get("/workallocation/case/task/#{caseId}")
        .headers(Headers.commonHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{xsrfToken}")
        .check(jsonPath("$[0].id").optional.saveAs("taskId"))
        .check(jsonPath("$[0].type").optional.saveAs("taskType")))

      .pause(5, 10) // Wait between retries

    //   // Log task Type
    //   .exec (session => {
    //     println(s"Current Task Type: ${session("taskType").as[String]}")
    //     session
    // })
    }

    .pause(Environment.constantthinkTime)

  val RequestRespondentEvidence =

    exec(_.setAll("todayDate" -> Common.getDate()))

    .group("XUI_IAC_RequestRespondentEvidence_EventTrigger") {
      exec(http("XUI_IAC_RequestRespondentEvidence_010_GetCaseTasks")
        .get("/case/IA/Asylum/#{caseId}/trigger/requestRespondentEvidence?tid=#{taskId}")
        .headers(Headers.commonHeader))

      .exec(http("XUI_IAC_RequestRespondentEvidence_010_EventTrigger")
        .get("/case/IA/Asylum/#{caseId}/trigger/requestRespondentEvidence")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.apiUserDetails)
      .exec(Common.monitoringTools)
      .exec(Common.isAuthenticated)

      .exec(http("XUI_IAC_RequestRespondentEvidence_010_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("content-type", "application/json")) 

      .exec(Common.profile)

      .exec(http("XUI_IAC_RequestRespondentEvidence_010_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/requestRespondentEvidence?ignore-warning=false")
        .headers(Headers.commonHeader) 
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.isAuthenticated)
    }

    .pause(Environment.constantthinkTime)
            
    .group("XUI_IAC_RequestRespondentEvidence_Validate") {
      exec(http("XUI_IAC_RequestRespondentEvidence_020_Validate")
        .post("/data/case-types/Asylum/validate?pageId=requestRespondentEvidencerequestRespondentEvidence")
        .headers(Headers.commonHeader) 
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/XUIrequestRespondentEvidence1.json")))

      .exec(Common.apiUserDetails)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_IAC_RequestRespondentEvidence_Submit") {
      exec(http("XUI_IAC_RequestRespondentEvidence_030_SubmitEvent")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader) 
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("xuiBodies/XUIrequestRespondentEvidence2.json")))

      .exec(Common.apiUserDetails)

      .exec(http("XUI_IAC_RequestRespondentEvidence_030_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(Headers.commonHeader) 
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))
   
      .exec(http("XUI_IAC_RequestRespondentEvidence_030_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader) 
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))
 
      .exec(Common.apiUserDetails)
    }
} 