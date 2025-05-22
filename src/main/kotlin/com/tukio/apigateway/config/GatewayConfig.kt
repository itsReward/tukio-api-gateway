package com.tukio.apigateway.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class GatewayConfig {

    @Bean
    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator {
        val services = mapOf(
            "venue-service" to "tukio-venue-service",
            "events-service" to "tukio-events-service",
            "user-service" to "tukio-user-service",
            "recommendation-service" to "tukio-recommendation-service",
            "gamification-service" to "tukio-gamification-service",
            "notification-service" to "tukio-notification-service"
        )
        
        val routes = builder.routes()
            // Add these routes to handle OpenAPI documentation from each service
            .route("user-service-docs") { r ->
                r.path("/user-service/v3/api-docs")
                    .filters { f -> f.rewritePath("/user-service/v3/api-docs", "/v3/api-docs") }
                    .uri("lb://tukio-user-service")
            }
            .route("events-service-docs") { r ->
                r.path("/events-service/v3/api-docs")
                    .filters { f -> f.rewritePath("/events-service/v3/api-docs", "/v3/api-docs") }
                    .uri("lb://tukio-events-service")
            }
            .route("venue-service-docs") { r ->
                r.path("/venue-service/v3/api-docs")
                    .filters { f -> f.rewritePath("/venue-service/v3/api-docs", "/v3/api-docs") }
                    .uri("lb://tukio-venue-service")
            }
            .route("recommendation-service-docs") { r ->
                r.path("/recommendation-service/v3/api-docs")
                    .filters { f -> f.rewritePath("/recommendation-service/v3/api-docs", "/v3/api-docs") }
                    .uri("lb://tukio-recommendation-service")
            }
            .route("gamification-service-docs") { r ->
                r.path("/gamification-service/v3/api-docs")
                    .filters { f -> f.rewritePath("/gamification-service/v3/api-docs", "/v3/api-docs") }
                    .uri("lb://tukio-gamification-service")
            }
            .route("notification-service") { r ->
                r.path("/api/notifications/**", "/api/notification-templates/**", "/api/notification-preferences/**")
                    .filters { f ->
                        f.rewritePath("/api/(?<segment>.*)", "/api/\${segment}")
                            .circuitBreaker { it.setName("notificationServiceCircuitBreaker") }
                            .requestRateLimiter { c ->
                                c.rateLimiter = RedisRateLimiter(5, 10)
                                c.keyResolver = ipAddressKeyResolver()
                            }
                    }
                    .uri("lb://tukio-notification-service")
            }

        // Add Swagger API Documentation Routes
        services.forEach { (servicePath, serviceUri) ->
            routes.route("${serviceUri}-api-docs") { r ->
                r.path("/v3/api-docs/$servicePath")
                    .filters { f ->
                        f.rewritePath("/v3/api-docs/$servicePath", "/v3/api-docs")
                    }
                    .uri("lb://$serviceUri")
            }
        }
        
        return routes.build()
    }

    @Bean
    fun ipAddressKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            Mono.just(exchange.request.remoteAddress?.address?.hostAddress ?: "unknown")
        }
    }
}