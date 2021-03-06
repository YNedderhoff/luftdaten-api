package xyz.nedderhoff.luftdatenapi

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class LuftdatenApiApplication

fun main(args: Array<String>) {
    SpringApplication.run(LuftdatenApiApplication::class.java, *args)
}