package scenarios

import java.text.SimpleDateFormat
import java.util.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import java.io.{BufferedWriter, FileWriter}

object xuiSearchChallengedAccess {

  val baseURL = Environment.xuiBaseURL

  val GlobalSearch =

    exec(http("XUI_GlobalSearch_010_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_GlobalSearch_010_Services")
			.get("/api/globalSearch/services")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_GlobalSearch_010_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))
		
		// .exec(http("XUI_GlobalSearch_010_JurisdictionsRead")
		// 	.get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
		// 	.headers(XUIHeaders.xuiMainHeader)
    //   .header("accept", "application/json, text/plain, */*")
    //   .header("content-type", "application/json"))

    .pause(Environment.constantthinkTime)

    .exec(http("XUI_GlobalSearch_020_Request")
			.post("/api/globalsearch/results")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/GlobalSearchRequest.json"))
      .check(jsonPath("$.results[*].processForAccess").optional.saveAs("accessRequired")))
            		
		.exec(http("XUI_GlobalSearch_020_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
		
		.exec(http("XUI_GlobalSearch_020_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .exec(http("XUI_GlobalSearch_020_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

    // .exec {
    //   session =>
    //     println(session)
    //     session
    // }

  val JudicialChallengedAccess =

    exec(_.set("currentDate", Common.getDate()))

    .exec(http("XUI_RequestChallengedAccess_Request")
			.post("/api/challenged-access-request")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/JudicialChallengedAccessRequest.json")))

    .exec(http("XUI_RequestChallengedAccess_UserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

  val ChallengedAccess =

    exec(_.set("currentDate", Common.getDate()))

    .exec(http("XUI_RequestChallengedAccess_Request")
			.post("/api/challenged-access-request")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "#{xsrfToken}")
			.body(ElFileBody("xuiBodies/ChallengedAccessRequest.json")))

    .exec(http("XUI_RequestChallengedAccess_UserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

  val ViewCase =

    exec(http("XUI_ViewCase_GetCase")
			.get("/cases/case-details/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"))

    .exec(http("XUI_ViewCase_ConfigurationUI")
			.get("/external/configuration-ui/")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("XUI_ViewCase_ConfigJson")
			.get("/assets/config/config.json")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
    
    .exec(http("XUI_ViewCase_T&C")
			.get("/api/configuration?configurationKey=termsAndConditionsEnabled")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
      
    .exec(http("XUI_ViewCase_ConfigUI")
			.get("/external/config/ui")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_ViewCase_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(jsonPath("$.userInfo.token").saveAs("bearerToken")))

  //   .exec(session => {
  //   println(session)
  //   session
  //  })

    .exec(http("XUI_ViewCase_MonitoringTools")
			.get("/api/monitoring-tools")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_ViewCase_IsAuthenticated")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_ViewCase_GetCase")
			.get("/data/internal/cases/#{caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

    .exec(http("XUI_ViewCase_WAJurisdictionsGet")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_ViewCase_ApiUserDetails")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("XUI_ViewCase_GetWorkAllocationTask")
      .get("/workallocation/case/task/#{caseId}")
      .headers(XUIHeaders.xuiMainHeader)
      .check(jsonPath("$[0].id").optional.saveAs("taskId")))
}