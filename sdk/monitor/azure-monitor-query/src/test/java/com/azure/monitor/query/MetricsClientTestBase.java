// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;

import java.util.Arrays;

public class MetricsClientTestBase extends TestProxyTestBase {

    static final String FAKE_RESOURCE_ID
        = "/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/rg/providers/Microsoft.Compute/virtualMachines/vm";
    protected String metricEndpoint;
    protected MetricsClientBuilder clientBuilder;
    protected ConfigurationClient configClient;
    private TokenCredential credential;

    @Override
    public void beforeTest() {
        metricEndpoint = Configuration.getGlobalConfiguration()
            .get("AZURE_MONITOR_METRICS_ENDPOINT", "https://westus.metrics.monitor.azure.com");
        credential = TestUtil.getTestTokenCredential(interceptorManager);

        MetricsClientBuilder clientBuilder = new MetricsClientBuilder().credential(credential);

        String appConfigEndpoint
            = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_ENDPOINT", "https://fake.azconfig.io");
        ConfigurationClientBuilder configClientBuilder
            = new ConfigurationClientBuilder().endpoint(appConfigEndpoint).credential(credential);

        if (getTestMode() == TestMode.PLAYBACK) {
            interceptorManager.addMatchers(
                new CustomMatcher().setIgnoredQueryParameters(Arrays.asList("starttime", "endtime", "api-version"))
                    .setComparingBodies(false)
                    .setExcludedHeaders(Arrays.asList("x-ms-content-sha256")));
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());

            configClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            interceptorManager.addMatchers(
                new CustomMatcher().setIgnoredQueryParameters(Arrays.asList("starttime", "endtime", "api-version"))
                    .setComparingBodies(false)
                    .setExcludedHeaders(Arrays.asList("x-ms-content-sha256")));
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());

            configClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        this.clientBuilder = clientBuilder.endpoint(metricEndpoint);
        this.configClient = configClientBuilder.buildClient();
    }
}
