package net.inherency.finances.external.mint

import net.inherency.finances.config.ConfigurationService
import net.inherency.finances.config.ConfigKey
import net.inherency.finances.domain.transaction.MintTransaction
import net.inherency.finances.domain.transaction.TransactionRepository
import net.inherency.finances.external.parseMintFileAmount
import net.inherency.finances.external.parseMintFileLocalDate
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Service
class MintClient(private val configs: ConfigurationService,
                 private val mintFileParser: MintFileParser,
                 private val transactionRepository: TransactionRepository) {

    companion object {
        const val MINT_START_LOGIN_BUTTON = "/html/body/div[1]/div/header/div/div[1]/div/div[3]/a"
        const val FORM_EMAIL_INPUT = "Email"
        const val FORM_PASSWORD_INPUT = "Password"
        const val FORM_SIGN_IN_BUTTON = "SignIn"
        const val EMAIL_ONLY_CSS_SELECTOR = "#ius-identifier"
        const val CONTINUE_AFTER_EMAIL_CSS_SELECTOR = "#ius-sign-in-submit-btn"
        const val PASSWORD_ONLY_CSS_SELECTOR = "#ius-sign-in-mfa-password-collection-current-password"
        const val CONTINUE_AFTER_PASSWORD_CSS_SELECTOR = "#ius-sign-in-mfa-password-collection-continue-btn"
    }

    private val log = LoggerFactory.getLogger(MintClient::class.java)
    private val chromeDriverSystemPropertyKey = "webdriver.chrome.driver"

    fun downloadAllTransactions(doDownloadFile: Boolean): List<MintTransaction> {
        if (doDownloadFile) {
            log.info("Config to wait for {} seconds.", configs.getString(ConfigKey.CHROME_WAIT_FOR_UPDATE_SECONDS))
            setChromeDriverLocation()
            val driver = createMintDriverInstance()
            try {
                navigateToMintPage(driver)
                completeSignInForm(driver)
                waitForTransactionRefreshToComplete(driver)
                downloadTransactionFile(driver)
                waitForDownloadCompletion()
                return reportDownloadedTransactionFile()
            } finally {
                closeChrome(driver)
            }
        } else {
            log.info("Not downloading file... Checking to see if there is already a downloaded file.")
            return try {
                reportDownloadedTransactionFile()
            } catch (e: Exception) {
                log.info("Could not use download file.  Using existing transactions in spreadsheet...")
                transactionRepository.listAllMintTransactions()
            }
        }
    }

    private fun reportDownloadedTransactionFile(): List<MintTransaction> {
        val downloadFilePath = getDownloadFilePath()
        return transformFileToMintTransactions(downloadFilePath)
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

    //TODO: Move this to its own service.
    fun deleteDownloadFile() {
        try {
            val downloadFile = getDownloadFilePath()
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
        log.info("Downloading mint tx file: {}", transactionDownloadLink)
        driver.get(transactionDownloadLink)
    }

    private fun navigateToMintPage(driver: ChromeDriver) {
        val mintPage = configs.getString(ConfigKey.MINT_LOGIN_PAGE)
        log.info("Loading mint page: {}", mintPage)
        driver.get(mintPage)
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
        driver.findElementByXPath(MINT_START_LOGIN_BUTTON).click()
        val email = configs.getString(ConfigKey.MINT_LOGIN_EMAIL)
        val password = configs.getString(ConfigKey.MINT_LOGIN_PASSWORD)
        try {
            driver.findElement(By.name(FORM_EMAIL_INPUT)).sendKeys(email)
            driver.findElement(By.name(FORM_PASSWORD_INPUT)).sendKeys(password)
            driver.findElement(By.name(FORM_SIGN_IN_BUTTON)).click()
        } catch (e: Exception) {
            try {
                log.info("Exception trying first input attempt... trying second method")
                driver.findElementByCssSelector(EMAIL_ONLY_CSS_SELECTOR).sendKeys(email)
                driver.findElementByCssSelector(CONTINUE_AFTER_EMAIL_CSS_SELECTOR).click()
                WebDriverWait(driver, 10).until { webDriver -> webDriver.findElement(
                    By.cssSelector(PASSWORD_ONLY_CSS_SELECTOR)) }
                driver.findElementByCssSelector(PASSWORD_ONLY_CSS_SELECTOR)
                    .sendKeys(password)
                driver.findElementByCssSelector(CONTINUE_AFTER_PASSWORD_CSS_SELECTOR).click()
            } catch (e2: Exception) {
                log.info("Exception trying second method... trying third")
                driver.get("https://accounts.intuit.com/index.html?offering_id=Intuit.ifs.mint&namespace_id=50000026&redirect_url=https%3A%2F%2Fmint.intuit.com%2Foverview.event%3Futm_medium%3Ddirect%26cta%3Dnav_login_dropdown%26ivid%3D241355d6-f922-46da-bac3-17a6dbbe7fad")
                driver.findElement(By.name(FORM_EMAIL_INPUT)).sendKeys(email)
                driver.findElement(By.name(FORM_PASSWORD_INPUT)).sendKeys(password)
                driver.findElement(By.name(FORM_SIGN_IN_BUTTON)).click()
            } catch (noMore: Exception) {
                log.error("Could not complete sign in for file download", e)
                log.info("Please press Enter to continue...")
                readLine()
                throw noMore
            }
        }
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

    private fun setChromeDriverLocation() {
        val driverLocation= configs.getString(ConfigKey.CHROME_WEB_DRIVER_LOCATION)
        log.info("Using chrome driver location: {}", driverLocation)
        System.setProperty(chromeDriverSystemPropertyKey, driverLocation)
    }
}