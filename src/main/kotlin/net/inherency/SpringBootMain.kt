package net.inherency

import com.google.api.client.json.jackson2.JacksonFactory
import net.inherency.config.ConfigurationService
import net.inherency.external.mint.MintClient
import net.inherency.external.mint.MintFileParser
import org.apache.commons.configuration.EnvironmentConfiguration
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

@SpringBootApplication
@ConfigurationPropertiesScan("net.inherency.config")
open class SpringBootMain {

    @Bean
    open fun jsonFactoryBean(): JacksonFactory {
        return JacksonFactory.getDefaultInstance()
    }

    @Bean
    open fun getEnvVars(): EnvVars {
        // For some strange reason, we have to use env.map instead of env.getString(...)
        // If we use env.getString(), sometimes, config values (mainly JSON keys) get cut off... not sure why.
        return EnvironmentConfiguration().map
    }
}

@Service
open class CommandLineRunnerK(
        private val configurationService: ConfigurationService): CommandLineRunner {

    private val log = LoggerFactory.getLogger(CommandLineRunnerK::class.java)

    override fun run(vararg args: String?) {
        val mint = MintClient(configurationService, MintFileParser())
        val txs = mint.downloadAllTransactions()

        log.info("Printing each transaction...")
        txs.forEach{
            log.info(it.toString())
        }
    }

}

typealias EnvVars = Map<String, Any>

fun main(args: Array<String>) {
    SpringApplication.run(SpringBootMain::class.java, *args)
}