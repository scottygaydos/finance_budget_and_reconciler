package net.inherency

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class SpringBootMain

fun main(args: Array<String>) {
    SpringApplication.run(SpringBootMain::class.java, *args)
    readLines()
}