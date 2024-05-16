// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.quantum.jobs.models.ProviderStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ProviderClientTest extends QuantumClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private ProvidersClient client;

    private void initializeClient(HttpClient httpClient) {
        client = getClientBuilder(httpClient).buildProvidersClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getStatusTest(HttpClient httpClient) {
        initializeClient(httpClient);

        PagedIterable<ProviderStatus> jobs = client.getStatus();
        AtomicInteger total = new AtomicInteger();

        //check constant values on all jobs
        jobs.forEach(status -> {
            assertNotEquals(null, status.getId());
            assertNotEquals(null, status.getTargets());
            assertNotEquals(null, status.getCurrentAvailability());
            total.getAndIncrement();
        });
        // should be at least one in the list
        assertTrue(total.intValue() >= 1);
    }
}
