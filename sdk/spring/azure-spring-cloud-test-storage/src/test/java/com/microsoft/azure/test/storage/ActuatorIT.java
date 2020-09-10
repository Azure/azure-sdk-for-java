// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.test.storage;


import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.azure.test.utils.AppRunner;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class ActuatorIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActuatorIT.class);
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final String AZURE_STORAGE_ACCESSKEY = System.getenv("AZURE_STORAGE_ACCESSKEY");
    private static final String AZURE_STORAGE_ACCOUNT = System.getenv("AZURE_STORAGE_ACCOUNT");
    private static final String AZURE_STORAGE_BLOB = System.getenv("AZURE_STORAGE_BLOB");
    private static final String AZURE_STORAGE_FILE = System.getenv("AZURE_STORAGE_FILE");

    @Test
    public void testBlobStorageActuatorHealth() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            //set properties
            app.property("spring.cloud.azure.storage.access-key", AZURE_STORAGE_ACCESSKEY);
            app.property("spring.cloud.azure.storage.account", AZURE_STORAGE_ACCOUNT);
            app.property("blob", AZURE_STORAGE_BLOB);
            app.property("management.endpoint.health.show-details", "always");
            app.property("management.endpoints.web.exposure.include", "*");
            //start app
            app.start();
            String response = null;
            int count = 3;
            while (count > 0) {
                try {
                    response = REST_TEMPLATE.getForObject(
                        "http://localhost:" + app.port() + "/actuator/health/blobStorage", String.class);
                    break;
                } catch (Exception e) {
                    count--;
                }
            }
            assertTrue(response != null && response.contains("\"status\":\"UP\""));
        }
    }

    @Test
    public void testFileStorageActuatorHealth() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            //set properties
            app.property("spring.cloud.azure.storage.access-key", AZURE_STORAGE_ACCESSKEY);
            app.property("spring.cloud.azure.storage.account", AZURE_STORAGE_ACCOUNT);
            app.property("file", AZURE_STORAGE_FILE);
            app.property("management.endpoint.health.show-details", "always");
            //start app
            app.start();
            String response = null;
            int count = 3;
            while (count > 0) {
                try {
                    response = REST_TEMPLATE.getForObject(
                        "http://localhost:" + app.port() + "/actuator/health/fileStorage", String.class);
                    break;
                } catch (Exception e) {
                    count--;
                }
            }
            assertTrue(response != null && response.contains("\"status\":\"UP\""));
        }
    }
}
