package net.inherency.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("app")
data class AppConfig (
        val chromeConfig: ChromeConfig,
        val mintConfig: MintConfig
)

data class ChromeConfig (
    val webDriverLocation: String,
    val webDriverDownloadFile: String,
    val waitForUpdateSeconds: String,
    val webDriverHeadless: String
)

data class MintConfig (
    val loginPage: String,
    val transactionDownloadLink: String
)