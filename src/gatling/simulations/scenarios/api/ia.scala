package scenarios

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import ccd._

object ia {

  val config: Config = ConfigFactory.load()

  val ccdCreateIACCase =

    exec(_.setAll(  "firstName"  -> ("Perf" + Common.randomString(5)),
      "lastName"  -> ("Test" + Common.randomString(5)),
      "dobDay" -> Common.getDay(),
      "dobMonth" -> Common.getMonth(),
      "dobYear" -> Common.getDobYear(),
      "todayDate" -> Common.getDate(),
      "todayYear" -> Common.getYear()))

    .exec(CcdHelper.createCase("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "startAppeal", "iacBodies/IACCreateCase.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "#{caseId}", "submitAppeal", "iacBodies/IACSubmitAppeal.json"))
    .exec(CcdHelper.addCaseEvent("#{email}", "#{password}", CcdCaseTypes.IA_Asylum, "#{caseId}", "submitAppeal", "iacBodies/IACRequestHomeOfficeData.json"))


}
