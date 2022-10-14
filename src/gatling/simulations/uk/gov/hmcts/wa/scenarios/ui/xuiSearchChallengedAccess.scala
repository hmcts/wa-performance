package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._
import java.io.{BufferedWriter, FileWriter}

object xuiSearchChallengedAccess {

  val baseURL = Environment.xuiBaseURL
  val IdamURL = Environment.idamURL

  val GlobalSearch =

    exec(http("request_0")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("request_1")
			.get("/api/globalSearch/services")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_2")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader))
		
		.exec(http("request_9")
			.get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .exec(http("request_10")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .pause(Environment.constantthinkTime)

    .exec(http("request_14")
			.post("/api/globalsearch/results")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(ElFileBody("xuiBodies/GlobalSearchRequest.json")))
            		
		.exec(http("request_17")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
		
		.exec(http("request_19")
			.get("/data/internal/cases/${caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json"))

    .exec(http("request_20")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

  val ChallengedAccess =

    exec(http("request_72")
			.post("/api/challenged-access-request")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("content-type", "application/json")
      .header("x-xsrf-token", "${xsrfToken}")
			.body(ElFileBody("xuiBodies/ChallengedAccessRequest.json")))

    .exec(http("request_73")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .pause(Environment.constantthinkTime)

  val ViewCase =

    exec(http("request_0")
			.get("/cases/case-details/${caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"))

    .exec(http("request_4")
			.get("/external/configuration-ui/")
			.headers(XUIHeaders.xuiMainHeader))

    .exec(http("request_6")
			.get("/assets/config/config.json")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
    
    .exec(http("request_7")
			.get("/api/configuration?configurationKey=termsAndConditionsEnabled")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
      
    .exec(http("request_8")
			.get("/external/config/ui")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("request_9")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(jsonPath("$.userInfo.token").saveAs("bearerToken")))

    .exec(session => {
    println(session)
    session
   })

    .exec(http("request_17")
			.get("/api/monitoring-tools")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("request_18")
			.get("/auth/isAuthenticated")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("request_21")
			.options(Environment.baseURL + "/activity/cases/0/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .check(status.in(200, 304, 403)))

    .exec(http("request_22")
			.get(Environment.baseURL + "/activity/cases/0/activity")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .header("authorization", "Bearer ${bearerToken}")
      .check(status.in(200, 304, 403)))

    .exec(http("request_25")
			.get("/data/internal/cases/${caseId}")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

    .exec(http("request_28")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))

    .exec(http("request_29")
			.get("/api/user/details")
			.headers(XUIHeaders.xuiMainHeader)
      .header("accept", "application/json, text/plain, */*"))
}