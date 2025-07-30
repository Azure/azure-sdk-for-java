// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;

import static com.azure.monitor.query.metrics.TestUtil.addTestProxySanitizersAndMatchers;

public class MetricsClientTestBase extends TestProxyTestBase {
    static final String FAKE_RESOURCE_ID
        = "/subscriptions/4d042dc6-fe17-4698-a23f-ec6a8d1e98f4/resourceGroups/rg/providers/Microsoft.Compute/virtualMachines/vm";
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
            addTestProxySanitizersAndMatchers(interceptorManager);
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());

            configClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            addTestProxySanitizersAndMatchers(interceptorManager);
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());

            configClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        this.clientBuilder = clientBuilder.endpoint(metricEndpoint);
        this.configClient = configClientBuilder.buildClient();
    }
}
