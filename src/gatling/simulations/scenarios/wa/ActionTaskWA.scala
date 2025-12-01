package scenarios.wa

import io.gatling.core.Predef._
import scenarios.wa.actions._

object ActionTaskWA {

  val debugMode = System.getProperty("debug", "off")

  val execute =

    exec(TaskManagement.SearchTask)

}