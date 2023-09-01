// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

public class MetricsBatchQueryTestBase extends TestProxyTestBase {

    static final String FAKE_RESOURCE_ID = "/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/rg/providers/Microsoft.Compute/virtualMachines/vm";
    protected String metricEndpoint;
    protected MetricsBatchQueryClientBuilder clientBuilder;
    protected ConfigurationClient configClient;

    @Override
    public void beforeTest() {
        metricEndpoint = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_METRICS_ENDPOINT", "https://westus.metrics.monitor.azure.com");

        MetricsBatchQueryClientBuilder clientBuilder = new MetricsBatchQueryClientBuilder();
        ConfigurationClientBuilder configClientBuilder = new ConfigurationClientBuilder();
        if (getTestMode() == TestMode.PLAYBACK) {
            interceptorManager.addMatchers(new CustomMatcher()
                .setIgnoredQueryParameters(Arrays.asList("starttime", "endtime", "api-version"))
                .setComparingBodies(false)
                .setExcludedHeaders(Arrays.asList("x-ms-content-sha256")));
            clientBuilder
                .credential(new MockTokenCredential())
                .httpClient(interceptorManager.getPlaybackClient());

            configClientBuilder
                .credential(new MockTokenCredential())
                .endpoint("https://fake.azconfig.io")
                .httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            interceptorManager.addMatchers(new CustomMatcher()
                .setIgnoredQueryParameters(Arrays.asList("starttime", "endtime", "api-version"))
                .setComparingBodies(false)
                .setExcludedHeaders(Arrays.asList("x-ms-content-sha256")));
            clientBuilder
                .addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());

            configClientBuilder
                .addPolicy(interceptorManager.getRecordPolicy())
                .connectionString(Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING"));
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(new DefaultAzureCredentialBuilder().build());
            configClientBuilder.connectionString(Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING"));

        }
        this.clientBuilder = clientBuilder.endpoint(metricEndpoint);
        this.configClient = configClientBuilder.buildClient();
    }
}
