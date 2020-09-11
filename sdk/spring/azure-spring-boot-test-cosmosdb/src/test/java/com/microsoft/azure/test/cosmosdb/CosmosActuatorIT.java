// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.test.cosmosdb;

import com.microsoft.azure.test.utils.AppRunner;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class CosmosActuatorIT {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final String AZURE_COSMOSDB_ENDPOINT = System.getenv("AZURE_COSMOSDB_ENDPOINT");
    private static final String AZURE_COSMOSDB_ACCOUNT_KEY = System.getenv("AZURE_COSMOSDB_ACCOUNT_KEY");
    private static final String AZURE_COSMOSDB_DATABASE_NAME = System.getenv("AZURE_COSMOSDB_DATABASE_NAME");

    @Test
    public void testCosmosSpringBootActuatorHealth() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            //set properties
            app.property("azure.cosmosdb.uri", AZURE_COSMOSDB_ENDPOINT);
            app.property("azure.cosmosdb.key", AZURE_COSMOSDB_ACCOUNT_KEY);
            app.property("azure.cosmosdb.database", AZURE_COSMOSDB_DATABASE_NAME);
            app.property("azure.cosmosdb.populateQueryMetrics", String.valueOf(true));
            app.property("management.endpoint.health.show-details", "always");
            app.property("management.health.azure-cosmos.enabled", "true");

            //start app
            app.start();

            final String response = REST_TEMPLATE.getForObject(
                "http://localhost:" + app.port() + "/actuator/health/cosmos", String.class);
            Assert.assertTrue(response != null && response.contains("\"status\":\"UP\""));
        }
    }
}
