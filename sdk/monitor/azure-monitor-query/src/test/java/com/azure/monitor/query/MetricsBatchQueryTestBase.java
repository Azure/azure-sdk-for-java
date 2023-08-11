// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class MetricsBatchQueryTestBase extends TestProxyTestBase {

    static final String FAKE_RESOURCE_ID = "/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/rg/providers/Microsoft.Compute/virtualMachines/vm";
    protected String metricEndpoint;
    protected MetricsBatchQueryClientBuilder clientBuilder;

    @Override
    public void beforeTest() {
        metricEndpoint = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_METRICS_ENDPOINT", "https://westus.metrics.monitor.azure.com");

        MetricsBatchQueryClientBuilder clientBuilder = new MetricsBatchQueryClientBuilder();
        if (getTestMode() == TestMode.PLAYBACK) {
            clientBuilder
                .credential(new MockTokenCredential())
                .httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                .addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        this.clientBuilder = clientBuilder.endpoint(metricEndpoint);
    }
}
