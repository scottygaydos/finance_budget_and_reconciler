package net.inherency.finances

import com.google.api.client.json.jackson2.JacksonFactory
import org.apache.commons.configuration.EnvironmentConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean

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

fun main(args: Array<String>) {
    //See ReconcileCommandLineRunner class for startup interaction
    SpringApplication.run(SpringBootMain::class.java, *args)
}