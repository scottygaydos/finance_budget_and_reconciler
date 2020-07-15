package net.inherency.config

import net.inherency.vo.Config
import org.apache.commons.configuration.EnvironmentConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.lang.IllegalStateException

@Configuration
@EnableConfigurationProperties(AppConfig::class)
open class ConfigurationService(
        private val environmentConfiguration: EnvironmentConfiguration,
        private val appConfig: AppConfig) {

    fun get(configKey: Config): String {
        val valueFromEnv = readFromEnv(configKey)
        return if (valueFromEnv.isNullOrBlank()) {
            readFromYml(configKey)
        } else {
            valueFromEnv
        }
    }

    private fun readFromYml(configKey: Config): String {
        val value = when (configKey) {
            Config.CHROME_WAIT_FOR_UPDATE_SECONDS -> appConfig.chromeConfig.waitForUpdateSeconds
            Config.CHROME_WEB_DRIVER_DOWNLOAD_FILE -> appConfig.chromeConfig.webDriverDownloadFile
            Config.CHROME_WEB_DRIVER_LOCATION -> appConfig.chromeConfig.webDriverLocation
            Config.MINT_LOGIN_PAGE -> appConfig.mintConfig.loginPage
            Config.MINT_TRANSACTION_DOWNLOAD_LINK -> appConfig.mintConfig.transactionDownloadLink
            else -> throw IllegalStateException("Sensitive configurations must be configured as ENV variables.")
        }
        if (value.isBlank()) {
            throw IllegalStateException("Missing configuration: $configKey")
        }
        return value
    }

    private fun readFromEnv(configKey: Config): String? {
        // For some strange reason, we have to use env.map instead of env.getString(...)
        // If we use env.getString(), sometimes, config values (mainly JSON keys) get cut off... why?
        return environmentConfiguration.map[configKey.name.toLowerCase()] as String?
    }
}