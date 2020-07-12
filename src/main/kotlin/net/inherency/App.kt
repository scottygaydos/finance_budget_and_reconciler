package net.inherency

import com.google.api.client.json.jackson2.JacksonFactory
import net.inherency.google.GoogleSheetClient
import org.apache.commons.configuration.EnvironmentConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object STATIC {
    val LOGGER: Logger = LoggerFactory.getLogger("App")
    val CONFIGS = readConfigurations()
}

/** INF classes and instances *************************************************/
// Every external configuration value should be enumerated here!
enum class Config {
    GMAIL_APP_NAME,
    AUTH_JSON,
    GOOGLE_SHEET_ID
}

fun jsonFactory(): JacksonFactory {
    return JacksonFactory.getDefaultInstance()
}

private fun environmentConfiguration(): EnvironmentConfiguration {
    return EnvironmentConfiguration()
}

fun readConfigurations(): Map<Config, String> {
    val parametersFromEnv = getParametersFromEnv()
    parametersFromEnv.forEach{(k, _) ->
        STATIC.LOGGER.info("Using configuration from ENV for $k")
    }
    return parametersFromEnv
}

private fun getParametersFromEnv(): Map<Config, String> {
    val env = environmentConfiguration()
    return Config.values()
            .map { config -> Pair(config, getParameterFromEnv(env, config)) }
            .filter { config -> config.second != null }
            .map { config -> Pair(config.first, config.second!!) }
            .toMap()
}

private fun getParameterFromEnv(env: EnvironmentConfiguration, config: Config): String? {
    // For some strange reason, we have to use env.map instead of env.getString(...)
    // If we use env.getString(), sometimes, config values (mainly JSON keys) get cut off... why?
    return env.map[config.name.toLowerCase()] as String?
}

private fun googleSheetClientForNonSpringBoot(): GoogleSheetClient {
    //Hard coded on purpose - we should never use Main in prod
    val devYml = "application.yml.dev"
    val file = File(Config::javaClass.javaClass.classLoader
            .getResource(devYml)!!.file)
    val spreadSheetId = file.readLines()
            .filter { it.contains("sheet_id") }
            .map {
                val firstAndSecond = it.split(":")
                Pair(firstAndSecond[0], firstAndSecond[1].trim())
            }
            .first()
            .second.replace("'", "")

    STATIC.LOGGER.info("Using dev spreadSheetId={}", spreadSheetId)
    return GoogleSheetClient(STATIC.CONFIGS, spreadSheetId)
}

fun readLines() {
    val lines = googleSheetClientForNonSpringBoot().listValues()
    lines.forEach{
        STATIC.LOGGER.info("line: {}", it)
    }
}

fun main() {
    STATIC.LOGGER.info("Running locally without Spring Boot")
    readLines()
}