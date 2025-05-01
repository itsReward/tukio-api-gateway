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
        return builder.routes()
            // Venue Service Routes
            .route("tukio-venue-service") { r ->
                r.path("/api/venues/**")
                    .filters { f ->
                        f.rewritePath("/api/venues/(?<segment>.*)", "/api/venues/\${segment}")
                            .circuitBreaker { it.setName("venueServiceCircuitBreaker") }
                            .requestRateLimiter { c ->
                                c.rateLimiter = RedisRateLimiter(5, 10)
                                c.keyResolver = ipAddressKeyResolver()
                            }
                    }
                    .uri("lb://tukio-venue-service")
            }

            // Event Service Routes
            .route("tukio-event-service") { r ->
                r.path("/api/events/**", "/api/event-categories/**", "/api/event-registrations/**")
                    .filters { f ->
                        f.rewritePath("/api/(?<segment>.*)", "/api/\${segment}")
                            .circuitBreaker { it.setName("eventServiceCircuitBreaker") }
                            .requestRateLimiter { c ->
                                c.rateLimiter = RedisRateLimiter(5, 10)
                                c.keyResolver = ipAddressKeyResolver()
                            }
                    }
                    .uri("lb://tukio-events-service")
            }

            // User Service Routes
            .route("tukio-user-service-api") { r ->
                r.path("/api/users/**")
                    .filters { f ->
                        f.rewritePath("/api/users/(?<segment>.*)", "/api/users/\${segment}")
                            .circuitBreaker { it.setName("userServiceCircuitBreaker") }
                            .requestRateLimiter { c ->
                                c.rateLimiter = RedisRateLimiter(3, 5)
                                c.keyResolver = ipAddressKeyResolver()
                            }
                    }
                    .uri("lb://tukio-user-service")
            }

            // Auth Routes
            .route("tukio-user-service-auth") { r ->
                r.path("/api/auth/**")
                    .filters { f ->
                        f.rewritePath("/api/auth/(?<segment>.*)", "/api/auth/\${segment}")
                            .circuitBreaker { it.setName("authServiceCircuitBreaker") }
                            .requestRateLimiter { c ->
                                c.rateLimiter = RedisRateLimiter(10, 20) // Higher limit for auth endpoints
                                c.keyResolver = ipAddressKeyResolver()
                            }
                    }
                    .uri("lb://tukio-user-service")
            }

            // Recommendation Service Routes
            .route("tukio-recommendation-service") { r ->
                r.path("/api/recommendations/**", "/api/preferences/**", "/api/activities/**")
                    .filters { f ->
                        f.rewritePath("/api/(?<segment>.*)", "/api/\${segment}")
                            .circuitBreaker { it.setName("recommendationServiceCircuitBreaker") }
                            .requestRateLimiter { c ->
                                c.rateLimiter = RedisRateLimiter(5, 10)
                                c.keyResolver = ipAddressKeyResolver()
                            }
                    }
                    .uri("lb://tukio-recommendation-service")
            }

            // Gamification Service Routes
            .route("tukio-gamification-service") { r ->
                r.path("/api/gamification/**", "/api/points/**", "/api/badges/**", "/api/leaderboards/**")
                    .filters { f ->
                        f.rewritePath("/api/(?<segment>.*)", "/api/\${segment}")
                            .circuitBreaker { it.setName("gamificationServiceCircuitBreaker") }
                            .requestRateLimiter { c ->
                                c.rateLimiter = RedisRateLimiter(5, 10)
                                c.keyResolver = ipAddressKeyResolver()
                            }
                    }
                    .uri("lb://tukio-gamification-service")
            }

            // Swagger API Documentation Routes
            .route("tukio-api-docs") { r ->
                r.path("/v3/api-docs/**")
                    .filters { f ->
                        f.rewritePath("/v3/api-docs/(?<service>.*)", "/v3/api-docs")
                    }
                    .uri("lb://tukio-\${service}")
            }
            .build()
    }

    @Bean
    fun ipAddressKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            Mono.just(exchange.request.remoteAddress?.address?.hostAddress ?: "unknown")
        }
    }
}