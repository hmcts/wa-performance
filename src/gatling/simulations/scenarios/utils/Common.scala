package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.jsonpath.JsonPathCheckType
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

object Common {

  /*======================================================================================
  * Common Utility Functions
  ======================================================================================*/

  val rnd = new Random()
  val now = LocalDate.now()
  val patternDay = DateTimeFormatter.ofPattern("dd")
  val patternMonth = DateTimeFormatter.ofPattern("MM")
  val patternYear = DateTimeFormatter.ofPattern("yyyy")
  val patternReference = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val patternTime = DateTimeFormatter.ofPattern("hh:mm:ss")

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getDay(): String = {
    (1 + rnd.nextInt(28)).toString.format(patternDay).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  def getMonth(): String = {
    (1 + rnd.nextInt(12)).toString.format(patternMonth).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  //Date of Birth >= 35 years
  def getDobYear(): String = {
    now.minusYears(35 + rnd.nextInt(70)).format(patternYear)
  }
  //Date of Birth <= 18 years
  def getDobYearChild(): String = {
    now.minusYears(2 + rnd.nextInt(15)).format(patternYear)
  }
  //Date of Death <= 21 years
  def getDodYear(): String = {
    now.minusYears(1 + rnd.nextInt(20)).format(patternYear)
  }

  //CurrentDate
  def getDate(): String = {
    now.format(patternReference)
  }

  //CurrentTime
  def getTime(): String = {
    now.format(patternTime)
  }

  //Saves partyId
  def savePartyId: CheckBuilder[JsonPathCheckType, JsonNode] = jsonPath("$.case_fields[*].value[*].value.party.partyId").saveAs("partyId")

  //Saves user ID
  def saveId: CheckBuilder[JsonPathCheckType, JsonNode] = jsonPath("$.case_fields[*].value[0].id").saveAs("id")

}