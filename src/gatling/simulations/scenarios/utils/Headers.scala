package utils

object Headers {

 val IdamUrl = Environment.idamURL

	val navigationHeader = Map(
		"accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
		"accept-encoding" -> "gzip, deflate, br",
		"accept-language" -> "en-GB,en;q=0.9",
		"sec-fetch-dest" -> "document",
		"sec-fetch-mode" -> "navigate",
		"sec-fetch-site" -> "same-origin",
		"sec-fetch-user" -> "?1",
		"upgrade-insecure-requests" -> "1")

	val commonHeader = Map(
		"accept-encoding" -> "gzip, deflate, br",
		"accept-language" -> "en-GB,en;q=0.9",
		"content-type" -> "application/json",
		"sec-fetch-dest" -> "empty",
		"sec-fetch-mode" -> "cors",
		"sec-fetch-site" -> "same-origin")

	val postHeader = Map(
		"content-type" -> "application/x-www-form-urlencoded"
	)

	val headers_logout = Map(
    "Pragma" -> "no-cache",
    "Sec-Fetch-Dest" -> "document",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "Sec-Fetch-User" -> "?1")

  //R2 Headers
  val xuiMainHeader = Map(
    // "accept" -> "*/*",
		"cache-control" -> "no-cache",
		"dnt" -> "1",
		"pragma" -> "no-cache",
    "origin" -> "https://manage-case.#{env}.platform.hmcts.net",
    "experimental" -> "true",
		"sec-ch-ua" -> """ Not A;Brand";v="99", "Chromium";v="96", "Google Chrome";v="96""",
		"sec-ch-ua-mobile" -> "?0",
		"sec-ch-ua-platform" -> "macOS",
		"sec-fetch-dest" -> "empty",
		"sec-fetch-mode" -> "cors",
		"sec-fetch-site" -> "same-origin",
    "request-id" -> "|/qDn7.xWuGp"
  )

  val cuiSTHeader = Map(
  		"accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
  		"accept-encoding" -> "gzip, deflate, br, zstd",
  		"accept-language" -> "en-US,en;q=0.9",
  		"priority" -> "u=0, i",
  		"sec-ch-ua" -> """Chromium";v="128", "Not;A=Brand";v="24", "Google Chrome";v="128""",
  		"sec-ch-ua-mobile" -> "?0",
  		"sec-ch-ua-platform" -> "macOS",
  		"sec-fetch-dest" -> "document",
  		"sec-fetch-mode" -> "navigate",
  		"sec-fetch-site" -> "none",
  		"sec-fetch-user" -> "?1",
  		"upgrade-insecure-requests" -> "1"
  )

  val cuiIdamHeader = Map(
  		"accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
  		"accept-encoding" -> "gzip, deflate, br, zstd",
  		"accept-language" -> "en-US,en;q=0.9",
  		"origin" -> "https://idam-web-public.perftest.platform.hmcts.net",
  		"priority" -> "u=0, i",
  		"sec-ch-ua" -> """Chromium";v="128", "Not;A=Brand";v="24", "Google Chrome";v="128""",
  		"sec-ch-ua-mobile" -> "?0",
  		"sec-ch-ua-platform" -> "macOS",
  		"sec-fetch-dest" -> "document",
  		"sec-fetch-mode" -> "navigate",
  		"sec-fetch-site" -> "same-origin",
  		"sec-fetch-user" -> "?1",
  		"upgrade-insecure-requests" -> "1"
  )

}