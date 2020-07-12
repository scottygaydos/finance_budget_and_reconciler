package net.inherency

import com.google.api.client.json.jackson2.JacksonFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
open class SpringBootMain {
    @Bean
    open fun jsonFactoryBean(): JacksonFactory {
        return jsonFactory()
    }
}


fun main(args: Array<String>) {
    SpringApplication.run(SpringBootMain::class.java, *args)
}