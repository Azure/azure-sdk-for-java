// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
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

        MetricsBatchQueryClientBuilder clientBuilder = new MetricsBatchQueryClientBuilder()
            .httpClient(getHttpClient(interceptorManager))
            .credential(getCredential());
        ConfigurationClientBuilder configClientBuilder = new ConfigurationClientBuilder()
            .httpClient(getHttpClient(interceptorManager))
            .credential(getCredential());

        if (getTestMode() == TestMode.PLAYBACK) {
            addCustomMatcher();

            configClientBuilder
                .endpoint("https://fake.azconfig.io");
        } else if (getTestMode() == TestMode.RECORD) {
            addCustomMatcher();
            clientBuilder
                .addPolicy(interceptorManager.getRecordPolicy());

            configClientBuilder
                .addPolicy(interceptorManager.getRecordPolicy());
        } else if (getTestMode() == TestMode.LIVE) {
            configClientBuilder.connectionString(Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING"));

        }
        this.clientBuilder = clientBuilder.endpoint(metricEndpoint);
        this.configClient = configClientBuilder.buildClient();
    }

    private static HttpClient getHttpClient(InterceptorManager interceptorManager) {
        HttpClient httpClient = interceptorManager.isPlaybackMode()
            ? interceptorManager.getPlaybackClient() : HttpClient.createDefault();

        httpClient = new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
        return httpClient;
    }

    private TokenCredential getCredential() {
        if (interceptorManager.isPlaybackMode()) {
            return new MockTokenCredential();
        } else {
            return new DefaultAzureCredentialBuilder().build();
        }
    }
    private void addCustomMatcher() {
        interceptorManager.addMatchers(new CustomMatcher()
            .setIgnoredQueryParameters(Arrays.asList("starttime", "endtime", "api-version"))
            .setComparingBodies(false)
            .setExcludedHeaders(Arrays.asList("x-ms-content-sha256")));
    }
}
