// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;

public abstract class MetricsAdvisorClientTestBase extends TestBase {

    @Override
    protected void beforeTest() {
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false)
            .assertAsync()
            .build();
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false)
            .assertSync()
            .build();
    }

    MetricsAdvisorClientBuilder getMetricsAdvisorBuilder(HttpClient httpClient,
                                                         MetricsAdvisorServiceVersion serviceVersion, boolean isSync) {
        HttpClient httpClient1;
        if (isSync) {
            httpClient1 = buildSyncAssertingClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);
        } else {
            httpClient1 = buildAsyncAssertingClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);
        }
        return getMetricsAdvisorBuilderInternal(httpClient1, serviceVersion, true);
    }

    MetricsAdvisorClientBuilder getMetricsAdvisorBuilderInternal(HttpClient httpClient,
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
