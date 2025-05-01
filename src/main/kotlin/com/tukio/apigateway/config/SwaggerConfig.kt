package com.tukio.apigateway.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteDefinition
import org.springframework.cloud.gateway.route.RouteDefinitionLocator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.ArrayList

@Configuration
class SwaggerConfig(private val routeDefinitionLocator: RouteDefinitionLocator) {

    @Value("\${spring.application.name}")
    private lateinit var applicationName: String

    @Bean
    fun apiDocConfig(): List<GroupedOpenApi> {
        val groups = ArrayList<GroupedOpenApi>()

        val allRoutes = routeDefinitionLocator.routeDefinitions.collectList().block() ?: emptyList()

        // Create a map of services to their API paths
        val servicePaths = mapOf(
            "tukio-venue-service" to listOf("/api/venues/**"),
            "tukio-events-service" to listOf("/api/events/**", "/api/event-categories/**", "/api/event-registrations/**"),
            "tukio-user-service" to listOf("/api/users/**", "/api/auth/**"),
            "tukio-recommendation-service" to listOf("/api/recommendations/**", "/api/preferences/**", "/api/activities/**"),
            "tukio-gamification-service" to listOf("/api/gamification/**", "/api/points/**", "/api/badges/**", "/api/leaderboards/**")
        )

        // Create OpenAPI group for each service
        for ((serviceName, paths) in servicePaths) {
            val matchingRoutes = allRoutes.filter { it.id.contains(serviceName) }
            if (matchingRoutes.isNotEmpty()) {
                groups.add(GroupedOpenApi.builder()
                    .pathsToMatch(*paths.toTypedArray())
                    .group(serviceName)
                    .build())
            }
        }

        // Add an API gateway group for all paths
        groups.add(GroupedOpenApi.builder()
            .pathsToMatch("/**")
            .group(applicationName)
            .build())

        return groups
    }
}