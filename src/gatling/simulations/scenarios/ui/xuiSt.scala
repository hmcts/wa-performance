package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object xuiSt {

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

    .exec(http("XUI_ST_SelectCaseTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(XUIHeaders.xuiMainHeader)
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

    // Loop until the task type matches "registerNewCase"
    .asLongAs(session => session("taskType").as[String] != "registerNewCase") {
      exec(http("XUI_ST_SelectCaseTaskRepeat")
        .get("/workallocation/case/task/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
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

  val EditCase = 

    exec(_.setAll("todayDate" -> Common.getDate()))

    .group("XUI_ST_EditCase_Page1") {
      exec(http("XUI_ST_EditCase_Page1_GetCaseTasks")
        .get("/cases/case-details/#{caseId}/trigger/edit-case?tid=#{taskId}")
        .headers(XUIHeaders.xuiMainHeader))
            
      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.configUI)
      .exec(Common.TsAndCs)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      
      .exec(http("XUI_ST_EditCase_Page1_GetTask")
        .get("/workallocation/case/tasks/#{caseId}/event/edit-case/caseType/CriminalInjuriesCompensation/jurisdiction/ST_CIC")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ST_EditCase_Page1_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_ST_EditCase_Page1_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/edit-case?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.isAuthenticated)  
      .exec(Common.profile)
    }
            
    .pause(Environment.constantthinkTime)
      
    .group("XUI_ST_EditCase_Page2") {
      exec(http("XUI_ST_EditCase_Page2")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-casecaseCategorisationDetails")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("stBodies/EditCasePage1.json")))
    }

      .pause(Environment.constantthinkTime)

      .group("XUI_ST_EditCase_Page3") {
        exec(http("XUI_ST_EditCase_Page3")
          .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-casedateObjects")
          .headers(XUIHeaders.xuiMainHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{xsrfToken}")
          .body(ElFileBody("stBodies/EditCasePage2.json")))
      }
          
      .pause(Environment.constantthinkTime)
        
      .group("XUI_ST_EditCase_Page4") {
        exec(http("XUI_ST_EditCase_Page4")
          .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-caseobjectSubjects")
          .headers(XUIHeaders.xuiMainHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/json")
          .header("x-xsrf-token", "#{xsrfToken}")
          .body(ElFileBody("stBodies/EditCasePage3.json")))
      }
        
    .pause(Environment.constantthinkTime)
      
    .exec(Common.postcodeLookup)
        
    .pause(Environment.constantthinkTime)
      
    .group("XUI_ST_EditCase_Page5"){
      exec(http("XUI_ST_EditCase_Page5")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-casesubjectDetailsObject")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("stBodies/EditCasePage4.json")))
    }
        
    .pause(Environment.constantthinkTime)
      
    .group("XUI_ST_EditCase_Page6"){
      exec(http("XUI_ST_EditCase_Page6")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-caseobjectContacts")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("stBodies/EditCasePage5.json")))
    }
        
    .pause(Environment.constantthinkTime)
      
    .group("XUI_ST_EditCase_Page7"){
      exec(http("XUI_ST_EditCase_Page7")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=edit-casefurtherDetailsObject")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("stBodies/EditCasePage6.json")))
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_EditCase_Submit"){
      exec(http("XUI_ST_EditCase_Submit_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json"))
          
      .exec(http("XUI_ST_EditCase_Submit_CaseEvent")
        .post("/data/cases/#{caseId}/events")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("stBodies/EditCasePage7.json")))

      .exec(http("XUI_ST_EditCase_Submit_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))

      .exec(http("XUI_ST_EditCase_Submit_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))
      
      .exec(Common.manageLabellingRoleAssignment)
      .exec(Common.waJurisdictions)
    }

  .pause(Environment.constantthinkTime)

  val BuildCase = 

    exec(http("XUI_ST_SelectCase")
      .get("/cases/case-details/#{caseId}/task")
      .headers(XUIHeaders.xuiMainHeader)
      .check(substring("HMCTS Manage cases")))

    .exec(http("XUI_ST_SelectCaseTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(XUIHeaders.xuiMainHeader)
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

    // Loop until the task type matches "vetNewCaseDocuments"
    .asLongAs(session => session("taskType").as[String] != "vetNewCaseDocuments") {
      exec(http("XUI_ST_SelectCaseTaskRepeat")
        .get("/workallocation/case/task/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
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

    .group("XUI_AssignTaskToMe") {
      exec(http("XUI_AssignTaskToMe_Claim")
        .post("/workallocation/task/#{taskId}/claim")
        .headers(XUIHeaders.xuiMainHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("{}")))
            
            
      .exec(http("XUI_AssignTaskToMe_PostTask")
        .post("/workallocation/case/task/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"refined":true}""")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.configUI)
      .exec(Common.TsAndCs)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
    }

    .pause(Environment.constantthinkTime)

    .group("XUI_ST_BuildCase_Page1") {
      exec(http("XUI_ST_BuildCase_Page1_GetTask")
        .get("/cases/case-details/#{caseId}/trigger/caseworker-case-built?tid=#{taskId}")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.configUI)
      .exec(Common.TsAndCs)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
              
      .exec(http("XUI_ST_BuildCase_Page1_GetCaseTasks")
        .get("/workallocation/case/tasks/#{caseId}/event/caseworker-case-built/caseType/CriminalInjuriesCompensation/jurisdiction/ST_CIC")
        .headers(XUIHeaders.xuiMainHeader))

      .exec(http("XUI_ST_BuildCase_Page1_EventTrigger")
        .get("/data/internal/cases/#{caseId}/event-triggers/caseworker-case-built?ignore-warning=false")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("eventToken")))

      .exec(Common.isAuthenticated)
      .exec(Common.profile)

      .exec(http("XUI_ST_BuildCase_Page1_Validate")
        .post("/data/case-types/CriminalInjuriesCompensation/validate?pageId=caseworker-case-builtcaseBuilt")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("stBodies/BuildCasePage1.json")))

      .exec(http("XUI_ST_BuildCase_Page1_GetTask")
        .get("/workallocation/task/#{taskId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json"))
    }

    .pause(Environment.constantthinkTime)
            
    .group("XUI_ST_BuildCase_Submit") {
      exec(http("XUI_ST_BuildCase_Submit_CaseEvent")
        .post("/data/cases/#{caseId}/events")
        .headers(XUIHeaders.xuiMainHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(ElFileBody("stBodies/BuildCasePage2.json")))

      .exec(http("XUI_ST_BuildCase_Submit_CompleteTask")
        .post("/workallocation/task/#{taskId}/complete")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("x-xsrf-token", "#{xsrfToken}")
        .body(StringBody("""{"actionByEvent":true,"eventName":"Case: Build case"}""")))

      .exec(http("XUI_ST_BuildCase_Submit_GetCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(XUIHeaders.xuiMainHeader)
        .header("content-type", "application/json")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))
    
      .exec(Common.waJurisdictions)
      .exec(Common.manageLabellingRoleAssignment)
    }
            




}