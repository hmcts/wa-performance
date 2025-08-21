package scenarios.st.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object cuiSpecialTribs {

  val cuiSTURL = Environment.cuiStURL

  val cuiHomePage = 

    exec(http("CUI_ST_010_HomePage")
			.get(cuiSTURL + "/")
			.headers(Headers.cuiSTHeader))

		.pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_020_LoginPage")
			.get(cuiSTURL + "/login")
			.headers(Headers.cuiSTHeader)
      .check(css("input[name='_csrf']", "value").saveAs("csrfToken")))
		
    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_030_Login")
			.post(Environment.idamURL + "/login?client_id=sptribs-frontend&response_type=code&redirect_uri=https://sptribs-frontend.#{env}.platform.hmcts.net/receiver")
			.headers(Headers.cuiIdamHeader)
			.formParam("username", "sptribs-citizen@mailinator.com")
			.formParam("password", "Pa55w0rd17")
			.formParam("selfRegistrationEnabled", "true")
			.formParam("_csrf", "#{csrfToken}")
      .check(CsrfCheck.save))
			
		.pause(Environment.constantthinkTime)

  val cuiCreateSTCase = 

		exec(http("CUI_ST_040_EnterSubjectDetails")
			.post(cuiSTURL + "/subject-details")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("subjectFullName", "Perf test")
			.formParam("subjectDateOfBirth-day", "01")
			.formParam("subjectDateOfBirth-month", "02")
			.formParam("subjectDateOfBirth-year", "1980")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_050_EnterContactDetails")
			.post(cuiSTURL + "/subject-contact-details")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("subjectEmailAddress", "perftest@mailinator.com")
			.formParam("subjectContactNumber", "07000111000")
			.formParam("subjectAgreeContact", "")
			.formParam("subjectAgreeContact", "Yes")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_060_SelectRepresentation")
			.post(cuiSTURL + "/representation")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("representation", "No")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_070_UploadAppealForm")
			.post(cuiSTURL + "/upload-appeal-form?_csrf=#{csrf}")
			.headers(Headers.cuiSTHeader)
      .header("content-type", "multipart/form-data")
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
      .formParam("_csrf", "#{csrf}")
      .bodyPart(RawFileBodyPart("documents", "3MB.pdf")
      .contentType("application/pdf")
      .fileName("3MB.pdf")
      .transferEncoding("binary")))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_080_SubmitAppealFormPage")
			.post(cuiSTURL + "/upload-appeal-form")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("documentUploadProceed", "true")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))

		.pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_090_UploadSupportingDocument")
			.post(cuiSTURL + "/upload-supporting-documents?_csrf=#{csrf}")
			.headers(Headers.cuiSTHeader)
      .header("content-type", "multipart/form-data")
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
      .formParam("_csrf", "#{csrf}")
      .bodyPart(RawFileBodyPart("documents", "3MB.pdf")
      .contentType("application/pdf")
      .fileName("3MB.pdf")
      .transferEncoding("binary")))

		.pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_100_SubmitSupportingDocumentPage")
			.post(cuiSTURL + "/upload-supporting-documents")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("documentUploadProceed", "true")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))

		.pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_110_AddOtherInformation")
			.post(cuiSTURL + "/upload-other-information?_csrf=#{csrf}")
			.headers(Headers.cuiSTHeader)
			.formParam("documentRelevance", "perf")
			.formParam("additionalInformation", "perf testing")
			.formParam("saveAndContinue", "")
      .check(CsrfCheck.save))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_120_SubmitCase")
			.post(cuiSTURL + "/check-your-answers")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
			.check(regex("Case Number:</font><br>(.+?)</strong>").transform(string => string.replace(" - ", "")).saveAs("caseId")))

    .pause(Environment.constantthinkTime)

    .exec(http("CUI_ST_130_Logout")
      .get(cuiSTURL + "/logout")
      .headers(Headers.cuiSTHeader))
}