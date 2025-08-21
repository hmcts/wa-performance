package scenarios.st

import io.gatling.core.Predef._
import scenarios.cuiSpecialTribs
import utils._

object CreateTaskST {

  val cuiSTURL = Environment.cuiStURL

  val execute = {

      exec(cuiSpecialTribs.cuiHomePage)
      .exec(cuiSpecialTribs.cuiCreateSTCase)
    }
}