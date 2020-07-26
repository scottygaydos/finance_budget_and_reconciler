package net.inherency.finances.config

import net.inherency.finances.config.*
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfigurationServiceTest {

    private val googleAppNameEnvVarValue = "finances"
    private val chromeWaitEnvVarValue = "31"

    private val chromeWebDriverLocationAppConfigValue = "C:\\driver_folder\\driver.exe"
    private val chromeDownloadLocationAppConfigValue = "C:\\Users\\username\\Downloads\\transactions.csv"
    private val chromeWaitAppConfigValue = "30"
    private val chromeWebDriverHeadless = "true"
    private val mintLoginPageAppConfigValue = "https://mint.com/login"
    private val mintTransactionDownloadLinkAppConfigValue = "https://mint.com/downloadTransactions"

    @Test
    fun `Configuration can be read from environment variables`() {
        val configurationService = initConfigurationService()
        val configValue = configurationService.getString(ConfigKey.GOOGLE_APP_NAME)
        assertEquals(googleAppNameEnvVarValue, configValue)
    }

    @Test
    fun `Configuration can be read from application yml fil - mint login page`() {
        val configurationService = initConfigurationService()
        val configValue = configurationService.getString(ConfigKey.MINT_LOGIN_PAGE)
        assertEquals(mintLoginPageAppConfigValue, configValue)
    }

    @Test
    fun `Configuration in environment variables overrides configuration in application yml file`() {
        val configurationService = initConfigurationService()
        val configValue = configurationService.getString(ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS)
        assertEquals(chromeWaitEnvVarValue, configValue)
    }

    @Test
    fun `AppConfig must find a configuration for chrome web driver`() {
        val configurationService = initConfigurationService()
        val configValue = configurationService.getString(ConfigKey.CHROME_WEB_DRIVER_LOCATION)
        assertEquals(chromeWebDriverLocationAppConfigValue, configValue)
    }

    @Test
    fun `AppConfig must find a configuration for chrome download location`() {
        val configurationService = initConfigurationService()
        val configValue = configurationService.getString(ConfigKey.CHROME_WEB_DRIVER_DOWNLOAD_FILE)
        assertEquals(chromeDownloadLocationAppConfigValue, configValue)
    }

    @Test
    fun `AppConfig must find a configuration for chrome wait time`() {
        val configurationService = initConfigurationServiceWithoutChromeWaitInEnvVariables()
        val configValue = configurationService.getString(ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS)
        assertEquals(chromeWaitAppConfigValue, configValue)
    }

    @Test
    fun `AppConfig must find a configuration for mint download page`() {
        val configurationService = initConfigurationService()
        val configValue = configurationService.getString(ConfigKey.MINT_TRANSACTION_DOWNLOAD_LINK)
        assertEquals(mintTransactionDownloadLinkAppConfigValue, configValue)
    }

    @Test
    fun `Expect exception when a configuration must be in environment variables, but has not be set`() {
        val configurationService = initConfigurationServiceWithoutGoogleAppNameInEnvVariables()
        assertFailsWith(IllegalStateException::class) {configurationService.getString(ConfigKey.GOOGLE_APP_NAME)}
    }

    @Test
    fun `Expect exception when a configuration in application yml is blank`() {
        val configurationService = initConfigurationServiceWithBlankMintLoginPageConfiguration()
        assertFailsWith(IllegalStateException::class) {configurationService.getString(ConfigKey.MINT_LOGIN_PAGE)}
    }

    private fun initConfigurationService(): ConfigurationService {
        return ConfigurationService(
                mapOf(
                        ConfigKey.GOOGLE_APP_NAME.name.toLowerCase() to googleAppNameEnvVarValue,
                        ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS.name.toLowerCase() to chromeWaitEnvVarValue
                ),
                AppConfig(
                        ChromeConfig(
                                chromeWebDriverLocationAppConfigValue,
                                chromeDownloadLocationAppConfigValue,
                                chromeWaitAppConfigValue,
                                chromeWebDriverHeadless),
                        MintConfig(
                                mintLoginPageAppConfigValue,
                                mintTransactionDownloadLinkAppConfigValue
                        )
                )
        )
    }

    private fun initConfigurationServiceWithBlankMintLoginPageConfiguration(): ConfigurationService {
        return ConfigurationService(
                mapOf(
                        ConfigKey.GOOGLE_APP_NAME.name.toLowerCase() to googleAppNameEnvVarValue,
                        ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS.name.toLowerCase() to chromeWaitEnvVarValue
                ),
                AppConfig(
                        ChromeConfig(
                                chromeWebDriverLocationAppConfigValue,
                                chromeDownloadLocationAppConfigValue,
                                chromeWaitAppConfigValue,
                                chromeWebDriverHeadless),
                        MintConfig(
                                " ",
                                mintTransactionDownloadLinkAppConfigValue
                        )
                )
        )
    }

    private fun initConfigurationServiceWithoutGoogleAppNameInEnvVariables(): ConfigurationService {
        return ConfigurationService(
                mapOf(
                        ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS.name.toLowerCase() to chromeWaitEnvVarValue
                ),
                AppConfig(
                        ChromeConfig(
                                chromeWebDriverLocationAppConfigValue,
                                chromeDownloadLocationAppConfigValue,
                                chromeWaitAppConfigValue,
                                chromeWebDriverHeadless),
                        MintConfig(
                                mintLoginPageAppConfigValue,
                                mintTransactionDownloadLinkAppConfigValue
                        )
                )
        )
    }

    private  fun initConfigurationServiceWithoutChromeWaitInEnvVariables(): ConfigurationService {
        return ConfigurationService(
                mapOf(
                        ConfigKey.GOOGLE_APP_NAME.name.toLowerCase() to googleAppNameEnvVarValue
                ),
                AppConfig(
                        ChromeConfig(
                                chromeWebDriverLocationAppConfigValue,
                                chromeDownloadLocationAppConfigValue,
                                chromeWaitAppConfigValue,
                                chromeWebDriverHeadless),
                        MintConfig(
                                mintLoginPageAppConfigValue,
                                mintTransactionDownloadLinkAppConfigValue
                        )
                )
        )
    }
}