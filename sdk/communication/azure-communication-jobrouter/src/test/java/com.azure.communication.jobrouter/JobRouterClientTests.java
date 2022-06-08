// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.RoundRobinMode;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobRouterClientTests extends JobRouterClientTestBase {
    private RouterClient routerClient;

    @Override
    protected void beforeTest() {
        routerClient = clientSetup(httpPipeline -> new RouterClientBuilder()
            .connectionString(getConnectionString())
            .pipeline(httpPipeline)
            .buildClient());
    }

    @Test
    public void createDistributionPolicy() {
        // Setup
        DistributionPolicy distributionPolicy = new DistributionPolicy();
        RoundRobinMode roundRobinMode = new RoundRobinMode();
        roundRobinMode.setMinConcurrentOffers(1);
        roundRobinMode.setMaxConcurrentOffers(10);
        distributionPolicy.setMode(roundRobinMode);
        distributionPolicy.setName("Test_Policy");
        distributionPolicy.setOfferTtlSeconds(10.0);

        // Action
        String id = "Contoso_Jobs_Distribution_policy";
        Response<DistributionPolicy> response = routerClient.upsertDistributionPolicyWithResponse(id, distributionPolicy);

        // Verify
        assertEquals(200, response.getStatusCode());
    }
}
