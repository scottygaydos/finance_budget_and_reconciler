package net.inherency.finances.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

@Suppress("unused")
class WireMockContextInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        const val LOGIN_PATH = "/login.html"
        const val ALT_LOGIN_PATH = "/favicon.ico"
        const val DOWNLOAD_PATH = "/download"
    }

    override fun initialize(ctx: ConfigurableApplicationContext) {
        val wireMock = createAndStartWireMockServerOnRandomPort()
        addWireMockSingletonToSpringToAllowAutoWiring(ctx, wireMock)
        listenForShutDownEventsToAutomaticallyAlsoStopWireMockServer(ctx, wireMock)
        interceptMintCalls(wireMock, ctx)
        stubMintLogin(wireMock)
        stubDownloadMintTransactionFileResponse(wireMock)
    }

    private fun interceptMintCalls(wireMock: WireMockServer, ctx: ConfigurableApplicationContext) {
        TestPropertyValues
                .of("app.mint-config.login-page=http://localhost:${wireMock.port()}$LOGIN_PATH")
                .and("app.mint-config.transaction-download-link=http://localhost:${wireMock.port()}$DOWNLOAD_PATH")
                .applyTo(ctx)
    }

    private fun listenForShutDownEventsToAutomaticallyAlsoStopWireMockServer(ctx: ConfigurableApplicationContext, wireMock: WireMockServer) {
        ctx.addApplicationListener {
            if (it is ContextClosedEvent) {
                wireMock.stop()
            }
        }
    }

    private fun addWireMockSingletonToSpringToAllowAutoWiring(ctx: ConfigurableApplicationContext, wireMock: WireMockServer) {
        ctx.beanFactory.registerSingleton("wireMock", wireMock)
    }

    private fun createAndStartWireMockServerOnRandomPort(): WireMockServer {
        val wireMock = WireMockServer(options().dynamicPort())
        wireMock.start()
        return wireMock
    }

    private fun stubMintLogin(wireMock: WireMockServer) {
        wireMock.stubFor(WireMock.get(LOGIN_PATH)
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, "text/html")
                                .withBody("<html><body><form><input name='Email' /><input name='Password' /><button name='SignIn' /></form></body></html>")
                )
        )
    }

    private fun stubDownloadMintTransactionFileResponse(wireMock: WireMockServer) {

        wireMock.stubFor(WireMock.get(DOWNLOAD_PATH)
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, "text/csv")
                                .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=transactions.csv")
                                .withBody(mintFileContent))
        )
    }
}
