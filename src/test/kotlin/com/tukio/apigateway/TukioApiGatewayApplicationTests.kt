package com.tukio.apigateway

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TukioApiGatewayApplicationTests {

    @Test
    fun contextLoads() {
        // This test validates that the Spring context loads successfully
    }
}