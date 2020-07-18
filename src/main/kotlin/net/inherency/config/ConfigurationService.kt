package net.inherency.config

import net.inherency.EnvVars
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.lang.IllegalStateException

@Configuration
@EnableConfigurationProperties(AppConfig::class)
open class ConfigurationService(
        private val envVars: EnvVars,
        private val appConfig: AppConfig) {

    fun get(configKeyKey: ConfigKey): String {
        val valueFromEnv = readFromEnv(configKeyKey)
        return if (valueFromEnv.isNullOrBlank()) {
            readFromYml(configKeyKey)
        } else {
            valueFromEnv
        }
    }

    private fun readFromYml(configKeyKey: ConfigKey): String {
        val value = when (configKeyKey) {
            ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS -> appConfig.chromeConfig.waitForUpdateSeconds
            ConfigKey.CHROME_WEB_DRIVER_DOWNLOAD_FILE -> appConfig.chromeConfig.webDriverDownloadFile
            ConfigKey.CHROME_WEB_DRIVER_LOCATION -> appConfig.chromeConfig.webDriverLocation
            ConfigKey.MINT_LOGIN_PAGE -> appConfig.mintConfig.loginPage
            ConfigKey.MINT_TRANSACTION_DOWNLOAD_LINK -> appConfig.mintConfig.transactionDownloadLink
            else -> throw IllegalStateException("Sensitive configurations must be configured as ENV variables.")
        }
        if (value.isBlank()) {
            throw IllegalStateException("Missing configuration: $configKeyKey")
        }
        return value
    }

    private fun readFromEnv(configKeyKey: ConfigKey): String? {
        return envVars[configKeyKey.name.toLowerCase()] as String?
    }
}