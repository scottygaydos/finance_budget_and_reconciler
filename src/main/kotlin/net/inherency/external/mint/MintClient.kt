package net.inherency.external.mint

import net.inherency.config.ConfigurationService
import net.inherency.vo.Config
import net.inherency.vo.MintTransaction
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Service
class MintClient(private val configs: ConfigurationService, private val mintFileParser: MintFileParser) {

    private val log = LoggerFactory.getLogger(MintClient::class.java)

    fun downloadAllTransactions(): List<MintTransaction> {
        log.info("Config to wait for {} seconds.", configs.get(Config.CHROME_WAIT_FOR_UPDATE_SECONDS))
        setChromeDriverLocation()
        val driver = createMintDriverInstance()
        navigateToMintPage(driver)
        completeSignInForm(driver)
        waitForTransactionRefreshToComplete(driver)
        downloadTransactionFile(driver)
        waitForDownloadCompletion()
        closeChrome(driver)
        val downloadFilePath = getDownloadFilePath()
        val transactions = transformFileToMintTransactions(downloadFilePath)
        deleteDownloadFile(downloadFilePath)
        return transactions
    }

    private fun closeChrome(driver: ChromeDriver) {
        driver.close()
    }

    private fun getDownloadFilePath(): String {
        val path = getConfig(Config.CHROME_WEB_DRIVER_DOWNLOAD_FILE)
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
        return mintFileParser.parseFile(File(downloadFilePath))
    }

    private fun waitForDownloadCompletion() {
        Thread.sleep(3000)
    }

    private fun downloadTransactionFile(driver: ChromeDriver) {
        val transactionDownloadLink = getConfig(Config.MINT_TRANSACTION_DOWNLOAD_LINK)
        driver.get(transactionDownloadLink)
    }

    private fun waitForTransactionRefreshToComplete(driver: ChromeDriver) {
        val waitForUpdateSeconds = getConfig(Config.CHROME_WAIT_FOR_UPDATE_SECONDS).toInt()
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
        val email = getConfig(Config.MINT_LOGIN_EMAIL)
        val password = getConfig(Config.MINT_LOGIN_PASSWORD)
        driver.findElement(By.name("Email")).sendKeys(email)
        driver.findElement(By.name("Password")).sendKeys(password)
        driver.findElement(By.name("SignIn")).click()
        log.info("Completed sign in with email = {} and password <...>", email)
    }

    private fun createMintDriverInstance(): ChromeDriver {
        return ChromeDriver()
    }

    private fun navigateToMintPage(driver: ChromeDriver) {
        val mintPage = getConfig(Config.MINT_LOGIN_PAGE)
        log.info("Loading mint page: {}", mintPage)
        driver.get(mintPage)
    }

    private fun setChromeDriverLocation() {
        val driverLocation= getConfig(Config.CHROME_WEB_DRIVER_LOCATION)
        log.info("Using chrome driver location: {}", driverLocation)
        System.setProperty("webdriver.chrome.driver", driverLocation)
    }

    private fun getConfig(config: Config): String {
        return configs.get(config)
    }
}