// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;

public abstract class MetricsAdvisorClientTestBase extends TestBase {

    static final String INCIDENT_ROOT_CAUSE_ID = "1516ffd506462aca05198391bb279aff-1746b031c00";
    static final String INCIDENT_ROOT_CAUSE_CONFIGURATION_ID = "59f26a57-55f7-41eb-8899-a7268d125557";

    @Override
    protected void beforeTest() {
    }

    MetricsAdvisorClientBuilder getMetricsAdvisorBuilder(HttpClient httpClient,
                                                         MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClientBuilder builder = new MetricsAdvisorClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new MetricsAdvisorKeyCredential("", ""));
        } else {
            builder.credential(
                new MetricsAdvisorKeyCredential(
                    Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_SUBSCRIPTION_KEY"),
                    Configuration.getGlobalConfiguration().get("AZURE_METRICS_ADVISOR_API_KEY")));
        }
        return builder;
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }
}
