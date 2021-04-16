package net.inherency.finances.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class CorsConfig : WebMvcConfigurer {


    override fun addCorsMappings(registry: CorsRegistry) {
        //registry.addMapping("/**")
                //.allowedOrigins("http://localhost:3000", "http://localhost:8080")
        registry.addMapping("/**").allowedMethods("*")
    }

}