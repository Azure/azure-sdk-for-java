// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.quantum.jobs.models.Quota;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class QuotasClientTest extends QuantumClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private QuotasClient client;

    private void initializeClient(HttpClient httpClient) {
        client = getClientBuilder(httpClient).buildQuotasClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void listTest(HttpClient httpClient) {
        initializeClient(httpClient);

        PagedIterable<Quota> quotas = client.list();
        AtomicInteger total = new AtomicInteger();

        //check constant values on all jobs
        quotas.forEach(quota -> {
            assertNotEquals(null, quota.getDimension());
            assertNotEquals(null, quota.getScope());
            assertNotEquals(null, quota.getProviderId());
            assertNotEquals(null, quota.getUtilization());
            assertNotEquals(null, quota.getHolds());
            assertNotEquals(null, quota.getLimit());
            assertNotEquals(null, quota.getPeriod());
            total.getAndIncrement();
        });
        // should be at least one in the list
        assertTrue(total.intValue() >= 2);
    }
}
