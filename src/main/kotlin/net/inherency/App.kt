package net.inherency

import com.google.api.client.json.jackson2.JacksonFactory
import net.inherency.google.GoogleSheetClient
import org.apache.commons.configuration.EnvironmentConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object STATIC {
    val LOGGER: Logger = LoggerFactory.getLogger("App")
    val CONFIGS = readConfigurations()
}

/** INF classes and instances *************************************************/
// Every external configuration value should be enumerated here!
enum class Config {
    GMAIL_APP_NAME,
    AUTH_JSON
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

fun readLines() {
    val drive = GoogleSheetClient(STATIC.CONFIGS)
    val lines = drive.listValues()
    lines.forEach{
        STATIC.LOGGER.info("line: {}", it)
    }
}

fun main() {
    readLines()
}