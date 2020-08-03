package net.inherency.finances

import com.google.api.client.json.jackson2.JacksonFactory
import net.inherency.finances.domain.reconcile.ReconcileService
import org.apache.commons.configuration.EnvironmentConfiguration
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.lang.Exception

@SpringBootApplication
@ConfigurationPropertiesScan("net.inherency.finances.config")
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

typealias EnvVars = Map<String, Any>


@Service
open class CommandLineRunnerK(private val reconcileService: ReconcileService): CommandLineRunner {

    private val log = LoggerFactory.getLogger(CommandLineRunnerK::class.java)

    override fun run(vararg args: String?) {
        try {
            val result = reconcileService.reconcile()
            log.info("There are ${result.unreconciledMintTransactions.size} unreconciled mint transactions remaining.")
            log.info("There are ${result.unreconciledCategorizedTransactions.size} " +
                    "unreconciled categorized transactions remaining.")
        } catch (e: Exception) {
            log.error("Could not reconcile in command line: ${e.localizedMessage}")
        }
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(SpringBootMain::class.java, *args)
}