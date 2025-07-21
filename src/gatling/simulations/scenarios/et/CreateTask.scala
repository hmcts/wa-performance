package scenarios.et

import ccd._
import io.gatling.core.Predef._
import utils._

object CreateTask {

  val feedETUserData = csv("ETUserData.csv").circular

  val execute = {

    feed(feedETUserData)
  }


}
