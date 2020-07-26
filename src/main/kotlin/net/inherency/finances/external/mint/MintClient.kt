package net.inherency.finances.external.mint

import net.inherency.finances.config.ConfigurationService
import net.inherency.finances.config.ConfigKey
import net.inherency.finances.domain.transaction.MintTransaction
import net.inherency.finances.external.parseMintFileAmount
import net.inherency.finances.external.parseMintFileLocalDate
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Service
class MintClient(private val configs: ConfigurationService, private val mintFileParser: MintFileParser) {

    companion object {
        const val FORM_EMAIL_INPUT = "Email"
        const val FORM_PASSWORD_INPUT = "Password"
        const val FORM_SIGN_IN_BUTTON = "SignIn"
    }

    private val log = LoggerFactory.getLogger(MintClient::class.java)
    private val chromeDriverSystemPropertyKey = "webdriver.chrome.driver"

    fun downloadAllTransactions(): List<MintTransaction> {
        log.info("Config to wait for {} seconds.", configs.getString(ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS))
        setChromeDriverLocation()
        val driver = createMintDriverInstance()
        try {
            navigateToMintPage(driver)
            completeSignInForm(driver)
            waitForTransactionRefreshToComplete(driver)
            downloadTransactionFile(driver)
            waitForDownloadCompletion()
        } finally {
            closeChrome(driver)
        }

        val downloadFilePath = getDownloadFilePath()
        val transactions = transformFileToMintTransactions(downloadFilePath)
        deleteDownloadFile(downloadFilePath)
        return transactions
    }

    private fun closeChrome(driver: ChromeDriver) {
        driver.close()
        driver.quit()
    }

    private fun getDownloadFilePath(): String {
        val path = configs.getString(ConfigKey.CHROME_WEB_DRIVER_DOWNLOAD_FILE)
        log.info("Using download path: {}", path)
        return path
    }

    private fun deleteDownloadFile(downloadFile: String) {
        try {
            Files.delete(Path.of(downloadFile))
            log.info("Deleted file: {}", downloadFile)
        } catch (e: Exception) {
            throw RuntimeException("Could not delete downloaded transactions.csv from Downloads.", e)
        }
    }

    private fun transformFileToMintTransactions(downloadFilePath: String): List<MintTransaction> {
        return mintFileParser.parseFile(
                File(downloadFilePath).readText(),
                { parseMintFileLocalDate(it) },
                { parseMintFileAmount(it) } )
    }

    private fun waitForDownloadCompletion() {
        Thread.sleep(3000)
    }

    private fun downloadTransactionFile(driver: ChromeDriver) {
        val transactionDownloadLink = configs.getString(ConfigKey.MINT_TRANSACTION_DOWNLOAD_LINK)
        driver.get(transactionDownloadLink)
    }

    private fun waitForTransactionRefreshToComplete(driver: ChromeDriver) {
        val waitForUpdateSeconds = configs.getInt(ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS)
        var foundRefreshText = false
        var attemptCounter = 0
        while (!foundRefreshText) {
            if (attemptCounter%5 == 0 && attemptCounter != 0) {
                log.info("Sleeping while waiting for mint refresh to complete. $attemptCounter")
            }
            Thread.sleep(1000)
            foundRefreshText = driver.pageSource.contains("Account refresh complete")
            attemptCounter++
            if (attemptCounter > waitForUpdateSeconds) {
                log.info("Cease waiting for refresh.")
                break
            }
        }
    }

    private fun completeSignInForm(driver: ChromeDriver) {
        val email = configs.getString(ConfigKey.MINT_LOGIN_EMAIL)
        val password = configs.getString(ConfigKey.MINT_LOGIN_PASSWORD)
        driver.findElement(By.name(FORM_EMAIL_INPUT)).sendKeys(email)
        driver.findElement(By.name(FORM_PASSWORD_INPUT)).sendKeys(password)
        driver.findElement(By.name(FORM_SIGN_IN_BUTTON)).click()
        log.info("Completed sign in with email = {} and password <...>", email)
    }

    private fun createMintDriverInstance(): ChromeDriver {
        return if (configs.getBoolean(ConfigKey.CHROME_WEB_DRIVER_HEADLESS)) {
            val downloadFilepath = "C:\\Users\\sgayd\\Downloads\\"
            val chromePreferences = HashMap<String, Any>()
            chromePreferences["profile.default_content_settings.popups"] = 0
            chromePreferences["download.prompt_for_download"] = "false"
            chromePreferences["download.default_directory"] = downloadFilepath

            val chromeOptions = ChromeOptions()
            chromeOptions.addArguments(
                    "--headless",
                    "--disable-gpu",
                    "--window-size=1920,1200",
                    "--ignore-certificate-errors",
                    "--start-maximized",
                    "--disable-infobars")
            chromeOptions.setExperimentalOption("prefs", chromePreferences)
            ChromeDriver(chromeOptions)
        } else {
            ChromeDriver()
        }

    }

    private fun navigateToMintPage(driver: ChromeDriver) {
        val mintPage = configs.getString(ConfigKey.MINT_LOGIN_PAGE)
        log.info("Loading mint page: {}", mintPage)
        driver.get(mintPage)
    }

    private fun setChromeDriverLocation() {
        val driverLocation= configs.getString(ConfigKey.CHROME_WEB_DRIVER_LOCATION)
        log.info("Using chrome driver location: {}", driverLocation)
        System.setProperty(chromeDriverSystemPropertyKey, driverLocation)
    }
}