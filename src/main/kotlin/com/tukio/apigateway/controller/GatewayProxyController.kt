package com.tukio.apigateway.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@Tag(name = "API Gateway Proxy", description = "Documentation for API Gateway routes")
class GatewayProxyController {

    @GetMapping("/services")
    @Operation(
        summary = "List available services",
        description = "Returns information about available microservices"
    )
    fun listServices() = mapOf(
        "services" to listOf(
            mapOf("name" to "user-service", "prefix" to "/user-service", "docs" to "/user-service/v3/api-docs"),
            mapOf("name" to "events-service", "prefix" to "/events-service", "docs" to "/events-service/v3/api-docs"),
            mapOf("name" to "venue-service", "prefix" to "/venue-service", "docs" to "/venue-service/v3/api-docs"),
            mapOf("name" to "recommendation-service", "prefix" to "/recommendation-service", "docs" to "/recommendation-service/v3/api-docs"),
            mapOf("name" to "gamification-service", "prefix" to "/gamification-service", "docs" to "/gamification-service/v3/api-docs")
        )
    )
    
    @GetMapping("/health")
    @Operation(
        summary = "Gateway Health Check",
        description = "Checks if the API Gateway is operational"
    )
    fun healthCheck() = mapOf(
        "status" to "UP",
        "gateway" to "Tukio API Gateway",
        "version" to "1.0"
    )
}