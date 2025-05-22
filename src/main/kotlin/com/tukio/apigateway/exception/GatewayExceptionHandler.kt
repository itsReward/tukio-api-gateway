package com.tukio.apigateway.exception

import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.cloud.gateway.support.NotFoundException
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Configuration
@Order(-2) // High precedence
class GatewayExceptionHandler : ErrorWebExceptionHandler {

    private val logger = LoggerFactory.getLogger(GatewayExceptionHandler::class.java)

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        logger.error("Gateway error: ${ex.message}", ex)

        val response = exchange.response
        response.headers.contentType = MediaType.APPLICATION_JSON

        val status = when (ex) {
            is ResponseStatusException -> ex.statusCode
            is NotFoundException -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        response.statusCode = status

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            path = exchange.request.uri.path,
            status = status.value(),
            error = status.toString().substringAfter(" ").trim(), // Extract reason phrase from status string
            message = ex.message ?: "An error occurred"
        )

        val buffer = response.bufferFactory().wrap(errorResponse.toJson().toByteArray())

        return response.writeWith(Mono.just(buffer))
    }

    private data class ErrorResponse(
        val timestamp: LocalDateTime,
        val path: String,
        val status: Int,
        val error: String,
        val message: String
    ) {
        fun toJson(): String {
            return """
                {
                    "timestamp": "$timestamp",
                    "path": "$path",
                    "status": $status,
                    "error": "$error",
                    "message": "$message"
                }
            """.trimIndent()
        }
    }
}