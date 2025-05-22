package com.tukio.apigateway.config

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SwaggerWebFluxConfig {
    @Bean
    fun groupedOpenApis(): List<GroupedOpenApi> {
        return listOf(
            GroupedOpenApi.builder()
                .group("api-gateway")
                .packagesToScan("com.tukio.apigateway")
                .pathsToMatch("/**")
                .addOpenApiCustomizer { openApi ->
                    // Manually add gateway routes to the OpenAPI specification
                    val paths = mutableMapOf<String, PathItem>()

                    // User service endpoints
                    addPathItem(paths, "/tukio-user-service/users", "User management operations")
                    addPathItem(paths, "/tukio-user-service/auth", "Authentication operations")

                    // Events service endpoints
                    addPathItem(paths, "/tukio-events-service/events", "Event management operations")
                    addPathItem(paths, "/tukio-events-service/categories", "Event categories")

                    // Venue service endpoints
                    addPathItem(paths, "/tukio-venue-service/venues", "Venue management")
                    addPathItem(paths, "/tukio-venue-service/bookings", "Venue booking operations")

                    // Add the paths to the OpenAPI specification
                    openApi.paths(paths as Paths?)
                }
                .build()
        )
    }

    private fun addPathItem(paths: MutableMap<String, PathItem>, path: String, description: String) {
        paths[path] = PathItem()
            .get(
                Operation()
                    .summary("Get $description")
                    .description("Gateway route to $path")
                    .addTagsItem(path.split("/")[1])
                    .responses(
                        ApiResponses()
                            .addApiResponse(
                                "200",
                                ApiResponse().description("Successful operation")
                            )
                    )
            )
            .post(
                Operation()
                    .summary("Create $description")
                    .description("Gateway route to $path")
                    .addTagsItem(path.split("/")[1])
                    .responses(
                        ApiResponses()
                            .addApiResponse(
                                "201",
                                ApiResponse().description("Successfully created")
                            )
                    )
            )
    }
}