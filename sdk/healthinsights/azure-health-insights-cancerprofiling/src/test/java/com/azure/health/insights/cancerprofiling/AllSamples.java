// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.cancerprofiling;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;

public class AllSamples {
    String endpoint;
    String apiKey;

    @BeforeEach
    public void init() {
        endpoint = System.getenv("SDK_JAVA_HDS_ENDPOINT");
        apiKey = System.getenv("SDK_JAVA_HDS_APIKEY");
    }

    // @Test
    public void inferCancerProfile() throws InterruptedException, IOException {
        SampleInferCancerProfile.runSample(endpoint, apiKey);
    }
}
