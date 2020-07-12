package net.inherency

import com.google.api.client.json.jackson2.JacksonFactory
import net.inherency.google.GoogleSheetClient
import org.apache.commons.configuration.EnvironmentConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

object STATIC {
    val LOGGER: Logger = LoggerFactory.getLogger("App")
    var CONFIGS = readConfigurationsEnvHasPriorityOverApplicationYmlFile()
}

fun main() {
    STATIC.LOGGER.info("Running locally without Spring Boot")
    readLinesNonSpringBoot()
}

/** INF classes and instances *************************************************/
// Every external configuration value should be enumerated here!
enum class Config {
    GMAIL_APP_NAME,
    AUTH_JSON,
    GOOGLE_SHEET_ID
}

enum class ConfigSource {
    ENV,
    PROP
}

private data class ConfigValue(
        val configSource: ConfigSource,
        val configKey: Config,
        val configValue: String
)

fun jsonFactory(): JacksonFactory {
    return JacksonFactory.getDefaultInstance()
}

@Service
class Configurations {
    fun get(configKey: Config): String? {
        return STATIC.CONFIGS[configKey]
    }
}

private fun environmentConfiguration(): EnvironmentConfiguration {
    return EnvironmentConfiguration()
}

private fun readConfigurationsEnvHasPriorityOverApplicationYmlFile(): Map<Config, String> {
    val parametersFromEnv = getParametersFromEnv()
    val parametersFromConfig = getParametersFromConfig()
    val configsToUse = parametersFromConfig
            .filterNot { config -> parametersFromEnv.map { it.configKey }.contains(config.configKey) }
            .toMutableList()
    configsToUse.addAll(parametersFromEnv)

    configsToUse.forEach { config ->
        STATIC.LOGGER.info("Using configuration from {} for key {}", config.configSource, config.configKey)
    }
    return configsToUse.map { Pair(it.configKey, it.configValue) }.toMap()
}

private fun getParametersFromConfig(): List<ConfigValue> {
    class ApplicationYml(
            @Value("\${google.sheet_id}") val googleSheetId: String
    )

    val yml = ApplicationYml("")
    return listOf(ConfigValue(ConfigSource.PROP, Config.GOOGLE_SHEET_ID, yml.googleSheetId))
}

private fun getParametersFromEnv(): List<ConfigValue> {
    val env = environmentConfiguration()
    return Config.values()
            .map { config -> Pair(config, getParameterFromEnv(env, config)) }
            .filter { config -> config.second != null }
            .map { config -> ConfigValue(ConfigSource.ENV, config.first, config.second!!) }
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
    val configMap = STATIC.CONFIGS.toMutableMap()
    configMap[Config.GOOGLE_SHEET_ID] = spreadSheetId
    STATIC.CONFIGS = configMap
    return GoogleSheetClient(Configurations(), jsonFactory())
}

private fun readLinesNonSpringBoot() {
    val lines = googleSheetClientForNonSpringBoot().listValues()
    lines.forEach{
        STATIC.LOGGER.info("line: {}", it)
    }
}