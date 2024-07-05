// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.redis.AzureJedisPasswordlessAutoConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@DisabledOnOs({OS.WINDOWS, OS.MAC})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AzureRedisAutoConfigurationTestContainerTest {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String REDIS_PASSWORD = "fake-testcontainer-password";

    /**
     * Pulling Docker registry name from testcontainers.properties file as prefix.
     */
    private static final GenericContainer<?> REDIS;

    static {
        System.out.println("TESTCONTAINERS_RYUK_DISABLED value: [" + System.getenv("TESTCONTAINERS_RYUK_DISABLED") + "]");

        REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:6"))
                .withCommand("--requirepass", REDIS_PASSWORD)
                .withExposedPorts(6379);
    }

    @BeforeAll
    public static void beforeAll() {
        REDIS.start();
    }

    @AfterAll
    public static void afterAll() {
        REDIS.stop();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", REDIS::getFirstMappedPort);
        registry.add("spring.redis.ssl", () -> false);
        registry.add("spring.redis.azure.passwordless-enabled", () -> true);
    }

    @Test
    @Order(1)
    void testSetAndGet() {

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("valueMap1", "map1");
        valueMap.put("valueMap2", "map2");
        valueMap.put("valueMap3", "map3");
        redisTemplate.opsForValue().multiSet(valueMap);
        String value = (String) redisTemplate.opsForValue().get("valueMap2");
        Assertions.assertEquals("map2", value);
    }

    @Test
    @Order(2)
    void testGetValueAfterSet() {
        String value = (String) redisTemplate.opsForValue().get("valueMap3");
        Assertions.assertEquals("map3", value);
    }

    @Configuration
    @Import({AzureJedisPasswordlessAutoConfiguration.class, RedisAutoConfiguration.class})
    static class AzureRedisPasswordlessTestConfig {

    }

    @Configuration
    static class SupplierConfig {

        @Bean
        AzureRedisCredentialSupplier azureRedisCredentialSupplier() {
            AzureAuthenticationTemplate template = mock(AzureAuthenticationTemplate.class);
            when(template.getTokenAsPassword()).thenReturn(REDIS_PASSWORD);
            return new AzureRedisCredentialSupplier(template);
        }
    }

}


