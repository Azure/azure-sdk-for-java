// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.test.keyvault;

import com.azure.spring.test.AppRunner;
import com.azure.spring.test.keyvault.app.DummyApp;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import static com.azure.spring.test.EnvironmentVariable.AZURE_KEYVAULT_URI;
import static com.azure.spring.test.EnvironmentVariable.SPRING_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.SPRING_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.SPRING_TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultActuatorIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultActuatorIT.class);
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootActuatorHealth() {
        LOGGER.info("testSpringBootActuatorHealth begin.");
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            app.property("spring.cloud.azure.keyvault.enabled", "true");
            app.property("spring.cloud.azure.keyvault.uri", AZURE_KEYVAULT_URI);
            app.property("spring.cloud.azure.keyvault.credential.client-id", SPRING_CLIENT_ID);
            app.property("spring.cloud.azure.keyvault.credential.client-secret", SPRING_CLIENT_SECRET);
            app.property("spring.cloud.azure.keyvault.credential.tenant-id", SPRING_TENANT_ID);
            app.property("management.endpoint.health.show-details", "always");
            app.property("management.endpoints.web.exposure.include", "*");
            app.property("management.health.azure-key-vault.enabled", "true");
            app.start();

            final String response = REST_TEMPLATE.getForObject(
                "http://localhost:" + app.port() + "/actuator/health/keyVault", String.class);
            assertEquals("{\"status\":\"UP\"}", response);
            LOGGER.info("response = {}", response);
        }
        LOGGER.info("testSpringBootActuatorHealth end.");
    }

    /**
     * Test the Spring Boot /actuator/env integration.
     */
    @Test
    public void testSpringBootActuatorEnv() {
        LOGGER.info("testSpringBootActuatorEnv begin.");
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            app.property("spring.cloud.azure.keyvault.enabled", "true");
            app.property("spring.cloud.azure.keyvault.uri", AZURE_KEYVAULT_URI);
            app.property("spring.cloud.azure.keyvault.credential.client-id", SPRING_CLIENT_ID);
            app.property("spring.cloud.azure.keyvault.credential.client-secret", SPRING_CLIENT_SECRET);
            app.property("spring.cloud.azure.keyvault.credential.tenant-id", SPRING_TENANT_ID);
            app.property("management.endpoint.health.show-details", "always");
            app.property("management.endpoints.web.exposure.include", "*");
            app.property("management.health.azure-key-vault.enabled", "true");
            app.start();

            final String response = REST_TEMPLATE.getForObject(
                "http://localhost:" + app.port() + "/actuator/env", String.class);
            assert response != null;
            LOGGER.info("response = {}", response);
            assertTrue(response.contains("azurekv"));
        }
        LOGGER.info("testSpringBootActuatorEnv end.");
    }
}
