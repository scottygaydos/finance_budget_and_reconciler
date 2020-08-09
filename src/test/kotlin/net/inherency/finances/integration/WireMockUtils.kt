package net.inherency.finances.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

const val LOGIN_PATH = "/index.html"
const val DOWNLOAD_PATH = "/transactionDownload.event"
const val PORT = 8090

fun stubMintLogin(wireMock: WireMockServer) {
    val loginHtml = """
        <html><body><form>
            <input name='Email' />
            <input name='Password' />
            <button name='SignIn' />
        </form></body></html>""".trimIndent()
    wireMock.stubFor(WireMock.get(UrlPattern(ContainsPattern(LOGIN_PATH), false))
            .willReturn(
                    WireMock.aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(HttpHeaders.CONTENT_TYPE, "text/html")
                            .withBody(loginHtml)
            )
    )
}

fun stubDownloadMintTransactionFileResponse(wireMock: WireMockServer, testFileName: String) {
    class FileLoader
    val resource = FileLoader().javaClass.classLoader.getResource("$testFileName/mint_download.csv")!!.readText()
    println("Using mint download file: ${System.lineSeparator()} $resource")
    wireMock.stubFor(WireMock.get(UrlPattern(ContainsPattern(DOWNLOAD_PATH), false))
            .willReturn(
                    WireMock.aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(HttpHeaders.CONTENT_TYPE, "text/csv")
                            .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=transactions.csv")
                            .withBody(resource))
    )
}