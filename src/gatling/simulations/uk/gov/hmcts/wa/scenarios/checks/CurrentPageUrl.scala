package uk.gov.hmcts.wa.scenarios.checks

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck
import jodd.lagarto.dom.NodeSelector
import io.gatling.http.check.url.CurrentLocationCheckType
//import io.gatling.core.check.extractor.css.CssCheckType
import io.gatling.core.check.css.CssCheckType

object CurrentPageUrl {
    def save: CheckBuilder[CurrentLocationCheckType,String,String] = currentLocation.saveAs("currentPageUrl")
    def currentPageTemplate: String = "${currentPageUrl}"
}