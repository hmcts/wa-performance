package utils

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.css.CssCheckType
import jodd.lagarto.dom.NodeSelector

object CsrfCheck {
  //making the csrf check optional for NFD, as the Final Order login doesn't return a csrf token
  def save: CheckBuilder[CssCheckType, NodeSelector] = css("input[name='_csrf']", "value").optional.saveAs("csrf")
}