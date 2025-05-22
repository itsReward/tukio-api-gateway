package com.tukio.apigateway.filter

import com.tukio.apigateway.security.JwtService
import com.tukio.apigateway.util.RouteValidator
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val routeValidator: RouteValidator,
    private val jwtService: JwtService
) : GlobalFilter {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request

        // Skip validation for open endpoints
        if (!routeValidator.isSecured().test(request)) {
            return chain.filter(exchange)
        }

        // Check for Authorization header
        if (!request.headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            logger.warn("Missing authorization header for path: ${request.path}")
            return Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authorization header"))
        }

        // Extract and validate JWT token
        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid authorization header format")
            return Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization header format"))
        }

        val token = authHeader.substring(7)
        try {
            // Extract claims from token
            val username = jwtService.extractUsername(token)
            val roles = jwtService.extractRoles(token)

            if (username.isNullOrEmpty() || jwtService.isTokenExpired(token)) {
                logger.warn("Invalid or expired JWT token")
                return Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token"))
            }

            // Add user information to headers for downstream services
            val modifiedRequest = addAuthorizationHeaders(request, username, roles)
            return chain.filter(exchange.mutate().request(modifiedRequest).build())
        } catch (e: Exception) {
            logger.error("Error validating JWT token", e)
            return Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"))
        }
    }



    private fun addAuthorizationHeaders(request: ServerHttpRequest, username: String, roles: List<String>): ServerHttpRequest {
        return request.mutate()
            .header("X-Auth-User", username)
            .header("X-Auth-Roles", roles.joinToString(","))
            .header(HttpHeaders.AUTHORIZATION, request.headers.getFirst(HttpHeaders.AUTHORIZATION))
            .build()
    }
}