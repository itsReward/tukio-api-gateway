package com.tukio.apigateway.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class ResilienceConfig {

    @Bean
    fun defaultCustomizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory> {
        return Customizer { factory ->
            factory.configureDefault { id ->
                Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(5)
                        .failureRateThreshold(50.0f)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build())
                    .build()
            }

            // Service-specific configurations
            factory.configure({ builder ->
                builder.circuitBreakerConfig(CircuitBreakerConfig.custom()
                    .slidingWindowSize(10)
                    .failureRateThreshold(40.0f)
                    .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3))
                        .build())
            }, "userServiceCircuitBreaker", "authServiceCircuitBreaker")

            factory.configure({ builder ->
                builder.circuitBreakerConfig(CircuitBreakerConfig.custom()
                    .slidingWindowSize(15)
                    .failureRateThreshold(45.0f)
                    .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(4))
                        .build())
            }, "venueServiceCircuitBreaker", "eventServiceCircuitBreaker")

            factory.configure({ builder ->
                builder.circuitBreakerConfig(CircuitBreakerConfig.custom()
                    .slidingWindowSize(20)
                    .failureRateThreshold(60.0f)
                    .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(10))
                        .build())
            }, "recommendationServiceCircuitBreaker", "gamificationServiceCircuitBreaker")
        }
    }
}