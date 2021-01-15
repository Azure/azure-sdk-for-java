// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkSession;
import org.junit.jupiter.api.Test;

public class SparkSessionClientTest extends SparkClientTestBase {

    private SparkSessionClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new SparkClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .sparkPoolName(getSparkPoolName())
            .buildSparkSessionClient());
    }

    /**
     * Tests that role assignments can be listed in the key vault.
     */
    @Test
    public void getSparkSession() {
        for (SparkSession expectedSparkSession : client.getSparkSessions().getSessions()) {
            SparkSession actualSparkSession = client.getSparkSession(expectedSparkSession.getId());
            validateSparkSession(expectedSparkSession, actualSparkSession);
        }
    }
}
