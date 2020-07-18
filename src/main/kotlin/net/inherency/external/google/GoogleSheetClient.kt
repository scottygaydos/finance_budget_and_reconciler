package net.inherency.external.google

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import net.inherency.config.ConfigurationService
import net.inherency.config.ConfigKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GoogleSheetClient(
        private val configs: ConfigurationService,
        private val jsonFactory: JacksonFactory) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun listValues(): MutableCollection<Any> {
        val range = "Transactions!A1:B"
        val response = buildSheetsWithCredentials()
                .spreadsheets()
                .values()[googleSheetId(), range]
                .execute()
        return response.values
    }

    private fun buildSheetsWithCredentials(): Sheets {
        val key= credentialsKey()

        try {
            val credential = GoogleCredential
                    .fromStream(key.byteInputStream())
                    .createScoped(googleSheetScopes())

            return Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory,
                    credential)
                    .setApplicationName(googleApplicationName())
                    .build()
        } catch (e: Exception) {
            log.info("Exception trying to use oauth2 key.  Prefix: ${key.removeRange(10, key.length-1)}")
            throw e
        }
    }

    private fun credentialsKey(): String {
        return getConfig(ConfigKey.GOOGLE_AUTH_JSON)
    }

    private fun googleApplicationName(): String {
        return getConfig(ConfigKey.GOOGLE_APP_NAME)
    }

    private fun googleSheetId(): String {
        return getConfig(ConfigKey.GOOGLE_SHEET_ID)
    }

    private fun getConfig(configKey: ConfigKey): String {
        return configs.get(configKey)
    }

    private fun googleSheetScopes(): List<String> {
        return listOf(SheetsScopes.SPREADSHEETS)
    }
}