// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;

public abstract class MetricsAdvisorClientTestBase extends TestBase {

    @Override
    protected void beforeTest() {
    }

    MetricsAdvisorClientBuilder getMetricsAdvisorBuilder(HttpClient httpClient,
                                                         MetricsAdvisorServiceVersion serviceVersion) {
        return getMetricsAdvisorBuilder(httpClient, serviceVersion, true);
    }

    MetricsAdvisorClientBuilder getMetricsAdvisorBuilder(HttpClient httpClient,
                                                         MetricsAdvisorServiceVersion serviceVersion,
                                                         boolean useKeyCredential) {
        MetricsAdvisorClientBuilder builder = new MetricsAdvisorClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"));
        } else {
            if (useKeyCredential) {
                builder.credential(new MetricsAdvisorKeyCredential(
                    Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_SUBSCRIPTION_KEY"),
                    Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_API_KEY")));
            } else {
                builder.credential(new DefaultAzureCredentialBuilder().build());
            }
        }
        return builder;
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }
}
