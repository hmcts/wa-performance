package uk.gov.hmcts.wa.scenarios

import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.wa.scenarios.utils._
import java.io.{BufferedWriter, FileWriter}

object xuiAllWork {

  val baseURL = Environment.xuiBaseURL
  val IdamURL = Environment.idamURL
  val taskListFeeder = csv("WA_TaskList.csv").circular
  val taskCancelListFeeder = csv("WA_TasksToCancel.csv").circular
  val feedIACUserData = csv("IACUserData.csv").circular
  val feedWASeniorUserData = csv("WA_SeniorTribunalUsers.csv").circular
  val feedWATribunalUserData = csv("WA_TribunalUsers.csv").circular
  val caseListFeeder = csv("WA_CaseList.csv").circular
  val feedCompleteTaskListFeeder = csv("WA_TasksToComplete.csv")
  val feedAssignTaskListFeeder = csv("WA_TasksToAssign.csv")
  val feedStaticTasksFeeder = csv("WA_StaticTasks.csv").random

  /*val AssignTask = 

    exec(http("request_0")
			.get("/api/healthCheck?path=%2Fwork%2Fall-work%2Ftasks")
			.headers(headers_0))

    .exec(http("request_1")
			.get("/api/user/details")
			.headers(headers_1))

    .exec(http("request_2")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(headers_2))
            
    .exec(http("request_3")
			.post("/workallocation2/task")
			.headers(headers_3)
			.body(RawFileBody("allWorkassignTask1_0003_request.txt")))

		.pause(4)

		.exec(http("request_4")
			.get("/api/user/details")
			.headers(headers_4))

		.pause(4)

		.exec(http("request_6")
			.get("/workallocation2/task/b1226682-7204-11ec-b835-528fa55456e5/roles")
			.headers(headers_6))

    .exec(http("request_8")
			.get("/workallocation2/task/b1226682-7204-11ec-b835-528fa55456e5")
			.headers(headers_8))

    .exec(http("request_9")
			.get("/api/user/details")
			.headers(headers_9))

		.pause(1)

		.exec(http("request_10")
			.get("/api/user/details")
			.headers(headers_10))

		.pause(9)

		.exec(http("request_11")
			.get("/api/user/details")
			.headers(headers_11))

		.pause(2)

		.exec(http("request_14")
			.post("/workallocation2/task/b1226682-7204-11ec-b835-528fa55456e5/assign")
			.headers(headers_14)
			.body(StringBody("""{"userId":"082654c0-f651-4cd1-9129-d8dd02de50ed"}""")))

    .exec(http("request_15")
			.get("/api/healthCheck?path=%2Fwork%2Fall-work%2Ftasks")
			.headers(headers_15))

    .exec(http("request_16")
			.get("/api/wa-supported-jurisdiction/get")
			.headers(headers_16))

    .exec(http("request_17")
			.get("/api/user/details")
			.headers(headers_17))

    .exec(http("request_18")
			.post("/workallocation2/task")
			.headers(headers_18)
			.body(ElFileBody("/resources/xuiBodies/XUISearchRequest.json")))

  */

}