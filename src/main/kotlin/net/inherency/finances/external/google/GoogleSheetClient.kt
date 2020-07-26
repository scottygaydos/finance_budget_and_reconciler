package net.inherency.finances.external.google

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ClearValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import net.inherency.finances.config.ConfigurationService
import net.inherency.finances.config.ConfigKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GoogleSheetClient(
        private val configs: ConfigurationService,
        private val jsonFactory: JacksonFactory) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun listValuesInTab(tabName: TabName): List<List<String>> {
        val range = tabName.toString()
        val response = buildSheetsWithCredentials()
                .spreadsheets()
                .values()[googleSheetId(), range]
                .execute()

        @Suppress("UNCHECKED_CAST")
        return response.values.toList()[2] as List<List<String>>
    }

    fun clearAllDataInTab(tabName: TabName) {
        val clearValuesRequest = ClearValuesRequest()
        buildSheetsWithCredentials().spreadsheets().values().clear(
                googleSheetId(),
                tabName.toString(),
                clearValuesRequest
        ).execute()
    }

    fun writeAllValuesToTab(tabName: TabName, values: List<List<String>>) {
        @Suppress("UNCHECKED_CAST")
        val appendBody = ValueRange().setValues(values as List<MutableList<Any>>?)

        val range = "$tabName!A1"
        buildSheetsWithCredentials().spreadsheets().values()
                .append(googleSheetId(), range, appendBody)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute()
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
        return configs.getString(configKey)
    }

    private fun googleSheetScopes(): List<String> {
        return listOf(SheetsScopes.SPREADSHEETS)
    }
}