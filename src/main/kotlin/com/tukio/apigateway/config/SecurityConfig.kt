package com.tukio.apigateway.config

import com.tukio.apigateway.util.RouteValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(private val routeValidator: RouteValidator) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeExchange { exchange ->
                exchange
                    .pathMatchers("/api/auth/**").permitAll()
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/v3/api-docs/**").permitAll()
                    .pathMatchers("/v3/api-docs.yaml").permitAll()
                    .pathMatchers("/swagger-ui/**").permitAll()
                    .pathMatchers("/webjars/**").permitAll()
                    .anyExchange().authenticated()
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type", "Accept")
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}