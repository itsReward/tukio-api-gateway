package com.tukio.apigateway.util

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import java.util.function.Predicate

@Component
class RouteValidator {

    companion object {
        // Define open endpoints that don't require authentication
        private val OPEN_API_ENDPOINTS = setOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/validate",
            "/actuator",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
        )
    }

    fun isSecured(): Predicate<ServerHttpRequest> {
        return Predicate { request ->
            val path = request.uri.path
            // Check if path is secured (not in open api endpoints)
            OPEN_API_ENDPOINTS.none { path.contains(it) }
        }
    }

    fun checkIfUriContainsOpenApiEndpoint(uri: String): Boolean {
        return OPEN_API_ENDPOINTS.any { uri.contains(it) }
    }
}