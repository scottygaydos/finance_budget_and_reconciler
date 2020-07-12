package net.inherency.google

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import net.inherency.Config
import net.inherency.jsonFactory
import org.slf4j.LoggerFactory

class GoogleSheetClient(private val configs: Map<Config, String>) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun listValues(): MutableCollection<Any> {
        val financesSheetId = "1JyhI0a8Q4MtPVDwT9nPsMCHtDqT3pan258ygEG5bQ6M"
        val range = "Transactions!A1:B"
        val response = login()
                .spreadsheets()
                .values()[financesSheetId, range]
                .execute()
        return response.values
    }

    private fun login(): Sheets {
        val key= credentialsKey()

        try {
            val credential = GoogleCredential
                    .fromStream(key.byteInputStream())
                    .createScoped(googleSheetScopes())

            return Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory(),
                    credential)
                    .setApplicationName(googleApplicationName())
                    .build()
        } catch (e: Exception) {
            log.info("Exception trying to use oauth2 key.  Prefix: ${key.removeRange(10, key.length-1)}")
            throw e
        }
    }

    private fun credentialsKey(): String {
        return getConfig(Config.AUTH_JSON)
    }

    private fun googleApplicationName(): String {
        return getConfig(Config.GMAIL_APP_NAME)
    }

    private fun getConfig(config: Config): String {
        return configs[config] ?: error("Could not find $config")
    }

    private fun googleSheetScopes(): List<String> {
        return listOf(SheetsScopes.SPREADSHEETS)
    }
}