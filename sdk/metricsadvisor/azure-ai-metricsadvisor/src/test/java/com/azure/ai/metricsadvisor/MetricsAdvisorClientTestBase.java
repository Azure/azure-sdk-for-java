// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static com.azure.ai.metricsadvisor.TestUtils.getEmailSanitizers;

public abstract class MetricsAdvisorClientTestBase extends TestProxyTestBase {

    @Override
    protected void beforeTest() {
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .build();
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }

    MetricsAdvisorClientBuilder getMetricsAdvisorBuilder(HttpClient httpClient,
                                                         MetricsAdvisorServiceVersion serviceVersion, boolean isSync) {
        HttpClient httpClient1 = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient;
        if (isSync) {
            httpClient1 = buildSyncAssertingClient(httpClient1);
        } else {
            httpClient1 = buildAsyncAssertingClient(httpClient1);
        }
        return getMetricsAdvisorBuilderInternal(httpClient1, serviceVersion, true);
    }

    MetricsAdvisorClientBuilder getMetricsAdvisorBuilderInternal(HttpClient httpClient,
                                                         MetricsAdvisorServiceVersion serviceVersion,
                                                         boolean useKeyCredential) {
        MetricsAdvisorClientBuilder builder = new MetricsAdvisorClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);

        interceptorManager.addSanitizers(getEmailSanitizers());
        if (useKeyCredential) {
            if (interceptorManager.isPlaybackMode()) {
                builder.credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"));
                // setting bodiless matcher to "exclude" matching request bodies with UUID's
                interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher(), new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("x-api-key"))));
            } else if (interceptorManager.isRecordMode()) {
                builder
                    .credential(new MetricsAdvisorKeyCredential(
                        Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_SUBSCRIPTION_KEY"),
                        Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_API_KEY")))
                    .addPolicy(interceptorManager.getRecordPolicy());
            }
        } else {
            if (interceptorManager.isPlaybackMode()) {
                builder.credential(new MockTokenCredential());
                // setting bodiless matcher to "exclude" matching request bodies with UUID's
                interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher()));
            } else if (interceptorManager.isRecordMode()) {
                builder
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .addPolicy(interceptorManager.getRecordPolicy());
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
