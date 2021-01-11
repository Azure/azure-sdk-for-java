// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.test.keyvault;

import com.azure.spring.test.management.ClientSecretAccess;
import com.azure.spring.test.AppRunner;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ActuatorIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActuatorIT.class);
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final String AZURE_KEYVAULT_URI = System.getenv("AZURE_KEYVAULT_URI");
    private static final ClientSecretAccess CLIENT_SECRET_ACCESS = ClientSecretAccess.load();

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootActuatorHealth() {
        LOGGER.info("testSpringBootActuatorHealth begin.");
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            app.property("azure.keyvault.enabled", "true");
            app.property("azure.keyvault.uri", AZURE_KEYVAULT_URI);
            app.property("azure.keyvault.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.tenant-id", CLIENT_SECRET_ACCESS.tenantId());
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
            app.property("azure.keyvault.enabled", "true");
            app.property("azure.keyvault.uri", AZURE_KEYVAULT_URI);
            app.property("azure.keyvault.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.tenant-id", CLIENT_SECRET_ACCESS.tenantId());
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
