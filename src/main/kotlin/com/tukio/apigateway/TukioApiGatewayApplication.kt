package com.tukio.apigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class TukioApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<TukioApiGatewayApplication>(*args)
}