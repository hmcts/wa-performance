package scenarios.st

import io.gatling.core.Predef._
import actions.cuiSpecialTribs
import utils._

object CreateTaskST {

  val execute =

      exec(IdamHelper.createCitizenUser)
      .exec(cuiSpecialTribs.cuiHomePage)
      .exec(cuiSpecialTribs.cuiCreateSTCase)
      .exec(IdamHelper.deleteUser)
}