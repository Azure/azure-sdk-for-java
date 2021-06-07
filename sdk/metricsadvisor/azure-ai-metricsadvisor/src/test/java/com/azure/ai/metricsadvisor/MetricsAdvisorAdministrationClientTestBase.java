// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;

public abstract class MetricsAdvisorAdministrationClientTestBase extends TestBase {

    @Override
    protected void beforeTest() {
    }

    MetricsAdvisorAdministrationClientBuilder getMetricsAdvisorAdministrationBuilder(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion) {
        return getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false);
    }

    MetricsAdvisorAdministrationClientBuilder getMetricsAdvisorAdministrationBuilder(HttpClient httpClient,
                                                                                     MetricsAdvisorServiceVersion serviceVersion,
                                                                                     boolean useKeyCredential) {
        MetricsAdvisorAdministrationClientBuilder builder = new MetricsAdvisorAdministrationClientBuilder()
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
