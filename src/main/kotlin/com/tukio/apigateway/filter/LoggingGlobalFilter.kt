package com.tukio.apigateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class LoggingGlobalFilter : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(LoggingGlobalFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val requestId = UUID.randomUUID().toString()

        // Log the incoming request
        logger.info(
            "Request: [{}] {} {} from {}",
            requestId,
            request.method,
            request.uri,
            request.remoteAddress?.address?.hostAddress ?: "unknown"
        )

        // Add a unique request ID to the headers for tracing
        val modifiedRequest = addRequestId(request, requestId)

        val startTime = System.currentTimeMillis()

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
            .then(Mono.fromRunnable {
                val duration = System.currentTimeMillis() - startTime
                logger.info(
                    "Response: [{}] {} {} completed in {}ms with status {}",
                    requestId,
                    request.method,
                    request.uri,
                    duration,
                    exchange.response.statusCode
                )
            })
    }

    private fun addRequestId(request: ServerHttpRequest, requestId: String): ServerHttpRequest {
        return request.mutate()
            .header("X-Request-ID", requestId)
            .build()
    }

    // Set high precedence (lower order value means higher precedence)
    override fun getOrder(): Int = -1
}