// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.test.storage;


import com.azure.spring.test.AppRunner;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import static com.azure.spring.test.EnvironmentVariable.AZURE_STORAGE_ACCOUNT_KEY;
import static com.azure.spring.test.EnvironmentVariable.AZURE_STORAGE_ACCOUNT_NAME;
import static com.azure.spring.test.EnvironmentVariable.AZURE_STORAGE_BLOB;
import static com.azure.spring.test.EnvironmentVariable.AZURE_STORAGE_BLOB_ENDPOINT;
import static com.azure.spring.test.EnvironmentVariable.AZURE_STORAGE_FILE;
import static com.azure.spring.test.EnvironmentVariable.AZURE_STORAGE_FILE_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageActuatorIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageActuatorIT.class);
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Test
    public void testBlobStorageActuatorHealth() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            //set properties
            app.property("spring.cloud.azure.storage.blob.account-name", AZURE_STORAGE_ACCOUNT_NAME);
            app.property("spring.cloud.azure.storage.blob.account-key", AZURE_STORAGE_ACCOUNT_KEY);
            app.property("spring.cloud.azure.storage.blob.endpoint", AZURE_STORAGE_BLOB_ENDPOINT);
            app.property("blob", AZURE_STORAGE_BLOB);
            app.property("management.endpoint.health.show-details", "always");
            app.property("management.health.azure-storage.enabled", "true");
            app.property("management.endpoints.web.exposure.include", "*");
            //start app
            app.start();
            String response = null;
            int count = 3;
            while (count > 0) {
                try {
                    response = REST_TEMPLATE.getForObject(
                        "http://localhost:" + app.port() + "/actuator/health/storageBlob", String.class);
                    break;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
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
            app.property("spring.cloud.azure.storage.fileshare.account-name", AZURE_STORAGE_ACCOUNT_NAME);
            app.property("spring.cloud.azure.storage.fileshare.account-key", AZURE_STORAGE_ACCOUNT_KEY);
            app.property("spring.cloud.azure.storage.fileshare.endpoint", AZURE_STORAGE_FILE_ENDPOINT);
            app.property("file", AZURE_STORAGE_FILE);
            app.property("management.health.azure-storage.enabled", "true");
            app.property("management.endpoint.health.show-details", "always");
            //start app
            app.start();

            String response = null;
            int count = 3;
            while (count > 0) {
                try {
                    response = REST_TEMPLATE.getForObject(
                        "http://localhost:" + app.port() + "/actuator/health/storageFile", String.class);
                    break;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    count--;
                }
            }
            assertTrue(response != null && response.contains("\"status\":\"UP\""));
        }
    }
}
