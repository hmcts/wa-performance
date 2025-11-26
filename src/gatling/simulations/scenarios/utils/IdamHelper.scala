package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

import scala.util.Random

object IdamHelper {

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val createCitizenUser =

    exec(_.setAll(
      "randomString" -> randomString(10),
      "password" -> "Password12"))

    .exec(http("IDAM_000_Create_Citizen_User")
      .post(Environment.idamAPI + "/testing-support/accounts")
      .header("Content-Type", "application/json")
      .body(ElFileBody("idam/CreateCitizenUser.json")).asJson
      .check(jsonPath("$.email").saveAs("email"))
      .check(status.is(201)))

  val deleteUser =

    exec(http("IDAM_000_Delete_Citizen_User")
      .delete(Environment.idamAPI + "/testing-support/accounts/#{email}")
      .header("Content-Type", "application/json")
      .header("Accept", "application/json"))

}
