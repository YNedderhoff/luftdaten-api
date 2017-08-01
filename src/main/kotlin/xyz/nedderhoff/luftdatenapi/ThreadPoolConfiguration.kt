package xyz.nedderhoff.luftdatenapi

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
open class ThreadPoolConfiguration {
    @Bean
    open fun executorService(): ExecutorService {
        return Executors.newFixedThreadPool(16)!!
    }
}