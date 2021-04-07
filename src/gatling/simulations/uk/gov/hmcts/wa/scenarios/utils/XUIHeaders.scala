package uk.gov.hmcts.wa.scenarios.utils

object XUIHeaders {

 val baseURL = Environment.baseURL
 val IdamUrl = Environment.idamURL

 val headers_0 = Map(
    "accept" -> "application/json, text/plain, */*",
    "accept-encoding" -> "gzip, deflate, br",
    "accept-language" -> "en-US,en;q=0.9",
    "sec-fetch-mode" -> "cors",
    "sec-fetch-site" -> "same-origin")

  val headers_1 = Map(
    "Pragma" -> "no-cache",
    "Sec-Fetch-Dest" -> "empty",
    "Sec-Fetch-Mode" -> "cors",
    "Sec-Fetch-Site" -> "same-origin")

  val headers_4 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
    "Pragma" -> "no-cache",
    "Sec-Fetch-Dest" -> "document",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_login_submit = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Accept-Language" -> "en-US,en;q=0.9",
    "Origin" -> IdamUrl,
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "Sec-Fetch-User" -> "?1",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_38 = Map(
    "accept" -> "application/json, text/plain, */*",
    "accept-encoding" -> "gzip, deflate, br",
    "accept-language" -> "en-US,en;q=0.9",
    "sec-fetch-dest" -> "empty",
    "sec-fetch-mode" -> "cors",
    "sec-fetch-site" -> "same-origin",
    "x-dtpc" -> "3$38732415_350h4vDTRMSASFKPLKDRFKMHCCHMMCARPGMHGD-0")

  val headers_access_read = Map(
    "accept" -> "application/json",
    "accept-encoding" -> "gzip, deflate, br",
    "accept-language" -> "en-US,en;q=0.9",
    "content-type" -> "application/json",
    "sec-fetch-dest" -> "empty",
    "sec-fetch-mode" -> "cors",
    "sec-fetch-site" -> "same-origin",
    "x-dtpc" -> "3$38734236_77h15vDTRMSASFKPLKDRFKMHCCHMMCARPGMHGD-0",
    "x-dtreferer" -> "https://manage-case.perftest.platform.hmcts.net/accept-terms-and-conditions")

  //Headers required for case specific journeys
  val headers_starteventtrigger = Map(
		"Accept" -> "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8",
		"Content-Type" -> "application/json",
		"DNT" -> "1",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|oBG4M.29AgL",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"experimental" -> "true",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

  val headers_apihealthcheck = Map(
		"Accept" -> "application/json, text/plain, */*",
		"DNT" -> "1",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|oBG4M.jwBh4",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

  val headers_userprofile = Map(
		"Accept" -> "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8",
		"Content-Type" -> "application/json",
		"DNT" -> "1",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|oBG4M.nBSu",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"experimental" -> "true",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

  val headers_casevalidate = Map(
		"Accept" -> "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8",
		"Content-Type" -> "application/json",
		"DNT" -> "1",
		"Origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|oBG4M.RQlmw",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"X-XSRF-TOKEN" -> "${xsrfToken}",
		"experimental" -> "true",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

  // val headers_55 = Map(
	// 	"Accept" -> "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8",
	// 	"Content-Type" -> "application/json",
	// 	"DNT" -> "1",
	// 	"Origin" -> "https://manage-case.perftest.platform.hmcts.net",
	// 	"Pragma" -> "no-cache",
	// 	"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
	// 	"Request-Id" -> "|oBG4M.I8RT+",
	// 	"Sec-Fetch-Dest" -> "empty",
	// 	"Sec-Fetch-Mode" -> "cors",
	// 	"Sec-Fetch-Site" -> "same-origin",
	// 	"X-XSRF-TOKEN" -> "${xsrfToken}",
	// 	"experimental" -> "true",
	// 	"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
	// 	"sec-ch-ua-mobile" -> "?0")

  val headers_createevent = Map(
		"Accept" -> "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8",
		"Content-Type" -> "application/json",
		"DNT" -> "1",
		"Origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|oBG4M.4ueKe",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"X-XSRF-TOKEN" -> "${xsrfToken}",
		"experimental" -> "true",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

  val headers_viewcase = Map(
		"Accept" -> "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json",
		"Content-Type" -> "application/json",
		"DNT" -> "1",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|oBG4M.b9v2B",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"experimental" -> "true",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

  //Task Manager Headers
  val headers_tm0 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Accept-Encoding" -> "gzip, deflate, br",
		"Accept-Language" -> "en-US,en;q=0.9",
		"Connection" -> "keep-alive",
		"Pragma" -> "no-cache",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

  val headers_tm10 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Content-Type" -> "application/json",
		"Origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"Pragma" -> "no-cache",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"X-XSRF-TOKEN" -> "${xsrfToken}",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	//Task List Headers
	val headers_tl1 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|fI9+e.+ZkFm",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	val headers_tl5 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Content-Type" -> "application/json",
		"Origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|fI9+e.ES04F",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"X-XSRF-TOKEN" -> "${xsrfToken}",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	//Assign Headers
	val headers_assign33 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Content-Type" -> "application/json",
		"DNT" -> "1",
		"Origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"Pragma" -> "no-cache",
		"Request-Id" -> "|Bj7Yl.ZaUtW",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"X-XSRF-TOKEN" -> "${xsrfToken}",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	val headers_assign35 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"DNT" -> "1",
		"Pragma" -> "no-cache",
		"Request-Id" -> "|Bj7Yl.mAc86",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	val openTaskHeader = Map(
		"Accept" -> "application/json, text/plain, */*",
		"DNT" -> "1",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|639Sn.6M0xB",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")
		
	//Cancel Task Headers
	val headers_ct24 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Content-Type" -> "application/json",
		"DNT" -> "1",
		"Origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|639Sn.ZGN+H",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"X-XSRF-TOKEN" -> "${xsrfToken}",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	val headers_ct25 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"DNT" -> "1",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|639Sn.18Vnc",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	val headers_ct26 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"DNT" -> "1",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|639Sn.IMzAf",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	val headers_ct31 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Content-Type" -> "application/json",
		"DNT" -> "1",
		"Origin" -> "https://manage-case.perftest.platform.hmcts.net",
		"Pragma" -> "no-cache",
		"Request-Context" -> "appId=cid-v1:7922b140-fa5f-482d-89b4-e66e9e6d675a",
		"Request-Id" -> "|639Sn.Shj0q",
		"Sec-Fetch-Dest" -> "empty",
		"Sec-Fetch-Mode" -> "cors",
		"Sec-Fetch-Site" -> "same-origin",
		"X-XSRF-TOKEN" -> "${xsrfToken}",
		"sec-ch-ua" -> """Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99""",
		"sec-ch-ua-mobile" -> "?0")

	val headers_logout = Map(
    "Pragma" -> "no-cache",
    "Sec-Fetch-Dest" -> "document",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "Sec-Fetch-User" -> "?1")
}