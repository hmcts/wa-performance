package scenarios.st.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

object cuiSpecialTribs {

  val cuiSTURL = Environment.cuiStURL

  val cuiHomePage = 

    exec(http("CUI_ST_010_HomePage")
			.get(cuiSTURL + "/")
			.headers(Headers.cuiSTHeader)
			.check(substring("Submit a First-tier Tribunal form")))
		.pause(Environment.constantthinkTime)

		.group("CUI_ST_020_SignInPage") {
			exec(http("CUI_ST_020_005_SignInPage")
				.get(cuiSTURL + "/login")
				.headers(Headers.cuiSTHeader)
				.check(substring("Sign in or create an account")))
		}

		/*======================================================================================
			Civil Citizen - Log in - Open Enter Email Page
			======================================================================================*/

		.group("CUI_ST_030_SignIn"){
			exec(http("CUI_ST_030_005_SignIn")
				.get(Environment.idamURL + "/enter-email")
				.headers(Headers.cuiIdamHeader)
				.check(CsrfCheck.save)
				.check(substring("Enter your email address")))
		}

		.pause(Environment.constantthinkTime)

		/*======================================================================================
		Civil Citizen - Log in - Validate Email
		======================================================================================*/

		.group("CUI_ST_040_AddEmail"){
			exec(http("CUI_ST_040_005_AddEmail")
				.post(Environment.idamURL + "/enter-email")
				.headers(Headers.cuiIdamHeader)
				.formParam("_csrf", "#{csrf}")
				.formParam("email", "#{email}")
				.check(CsrfCheck.save)
				.check(substring("Enter your password")))
		}
		.pause(Environment.constantthinkTime)

		/*======================================================================================
		Civil Citizen - Log in - Validate Password and log in
		======================================================================================*/

		.group("CUI_ST_050_AddPassword"){
			exec(http("CUI_ST_050_005_AddPassword")
				.post(Environment.idamURL + "/enter-password")
				.headers(Headers.cuiIdamHeader)
				.formParam("_csrf", "#{csrf}")
				.formParam("action","_submit")
				.formParam("password", "#{password}")
				.check(CsrfCheck.save)
				.check(substring("Enter your HMCTS reference number")))
		}

		.pause(Environment.constantthinkTime)

  val cuiCreateSTCase =

		exec(http("CUI_ST_060_StartNewAppeal")
			.post(cuiSTURL + "/cica-lookup")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("ccdReference", "")
			.formParam("cancel", "true")
			.check(CsrfCheck.save)
			.check(substring("Who is the subject")))

		.exec(http("CUI_ST_070_EnterSubjectDetails")
			.post(cuiSTURL + "/subject-details")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("subjectFullName", "Perf test")
			.formParam("subjectDateOfBirth-day", "01")
			.formParam("subjectDateOfBirth-month", "02")
			.formParam("subjectDateOfBirth-year", "1980")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
			.check(substring("Enter contact information")))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_080_EnterContactDetails")
			.post(cuiSTURL + "/subject-contact-details")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("subjectEmailAddress", "#{email}")
			.formParam("subjectContactNumber", "07000111000")
			.formParam("subjectAgreeContact", "")
			.formParam("subjectAgreeContact", "Yes")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
			.check(substring("Is there a representative named")))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_069_SelectRepresentation")
			.post(cuiSTURL + "/representation")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("representation", "No")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
			.check(substring("Enter your Criminal Injuries Compensation Authority Reference Number")))

		.pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_100_EnterCICNumber")
			.post(cuiSTURL + "/cica-reference-number")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("cicaReferenceNumber", "X12345")
			.formParam("saveAndContinue", "true")
			.check(CsrfCheck.save)
			.check(substring("Enter the date of your Criminal Injuries Compensation Authority")))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_110_EnterCICDecisionDate")
			.post(cuiSTURL + "/cica-decision-date")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("initialCicaDecisionDate-day", "10")
			.formParam("initialCicaDecisionDate-month", "03")
			.formParam("initialCicaDecisionDate-year", "2026")
			.formParam("saveAndContinue", "true")
			.check(CsrfCheck.save)
			.check(substring("Upload tribunal form")))

		.pause(Environment.constantthinkTime)

			//new page for entering date

		.exec(http("CUI_ST_120_UploadAppealForm")
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

		.exec(http("CUI_ST_130_SubmitAppealFormPage")
			.post(cuiSTURL + "/upload-appeal-form")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("documentUploadProceed", "true")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
			.check(substring("Upload supporting documents")))

		.pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_140_UploadSupportingDocument")
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

		.exec(http("CUI_ST_150_SubmitSupportingDocumentPage")
			.post(cuiSTURL + "/upload-supporting-documents")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("documentUploadProceed", "true")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
			.check(substring("Add information to an appeal")))

		.pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_160_AddOtherInformation")
			.post(cuiSTURL + "/upload-other-information?_csrf=#{csrf}")
			.headers(Headers.cuiSTHeader)
			.formParam("documentRelevance", "perf")
			.formParam("additionalInformation", "perf testing")
			.formParam("saveAndContinue", "")
      .check(CsrfCheck.save)
			.check(substring("Check your answers before submitting your tribunal form")))

    .pause(Environment.constantthinkTime)

		.exec(http("CUI_ST_170_SubmitCase")
			.post(cuiSTURL + "/check-your-answers")
			.headers(Headers.cuiSTHeader)
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
			.check(regex("Case Number:</font><br>(.+?)</strong>").transform(string => string.replace(" - ", "")).saveAs("caseId"))
			.check(substring("Tribunal form sent")))

    .pause(Environment.constantthinkTime)

    .exec(http("CUI_ST_180_Logout")
      .get(cuiSTURL + "/logout")
      .headers(Headers.cuiSTHeader)
			.header("path", "/logout")
			.check(substring("Submit a First-tier Tribunal form")))
}