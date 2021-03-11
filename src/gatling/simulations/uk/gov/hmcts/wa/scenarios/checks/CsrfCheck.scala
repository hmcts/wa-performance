package uk.gov.hmcts.wa.scenarios.checks

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.css.CssCheckType
//import io.gatling.core.check.extractor.css.CssCheckType
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck
import jodd.lagarto.dom.NodeSelector

object CsrfCheck {
  def save: CheckBuilder[CssCheckType, NodeSelector, String] = css("input[name='_csrf']", "value").saveAs("csrf")

  def csrfParameter: String = "_csrf"
  def csrfTemplate: String = "${csrf}"
}