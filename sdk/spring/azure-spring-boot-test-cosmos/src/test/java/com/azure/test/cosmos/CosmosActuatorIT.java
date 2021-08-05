// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.cosmos;

import com.azure.spring.test.AppRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class CosmosActuatorIT {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final String AZURE_COSMOS_ENDPOINT = System.getenv("AZURE_COSMOS_ENDPOINT");
    private static final String AZURE_COSMOS_ACCOUNT_KEY = System.getenv("AZURE_COSMOS_ACCOUNT_KEY");
    private static final String AZURE_COSMOS_DATABASE_NAME = System.getenv("AZURE_COSMOS_DATABASE_NAME");

    @Test
    public void testCosmosSpringBootActuatorHealth() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            //set properties
            app.property("azure.cosmos.uri", AZURE_COSMOS_ENDPOINT);
            app.property("azure.cosmos.key", AZURE_COSMOS_ACCOUNT_KEY);
            app.property("azure.cosmos.database", AZURE_COSMOS_DATABASE_NAME);
            app.property("azure.cosmos.populateQueryMetrics", String.valueOf(true));
            app.property("management.endpoint.health.show-details", "always");
            app.property("management.health.azure-cosmos.enabled", "true");

            //start app
            app.start();

            final String response = REST_TEMPLATE.getForObject(
                "http://localhost:" + app.port() + "/actuator/health/cosmos", String.class);
            Assertions.assertTrue(response != null && response.contains("\"status\":\"UP\""));
        }
    }
}
