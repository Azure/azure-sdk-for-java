// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.RootCause;
import com.azure.ai.metricsadvisor.implementation.util.IncidentRootCauseTransforms;
import com.azure.ai.metricsadvisor.models.IncidentRootCause;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;

import java.util.Collections;
import java.util.HashMap;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class IncidentRootCauseTestBase extends TestBase {

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

    static IncidentRootCause getExpectedIncidentRootCause() {
        RootCause innerRootCause = new RootCause()
            .setRootCause(new DimensionGroupIdentity().setDimension(new HashMap<String, String>() {
                {
                    put("category", "Shoes Handbags & Sunglasses");
                    put("city", "Chicago");
                }
            }))
            .setPath(Collections.singletonList("category"))
            .setScore(0.23402075133615907)
            .setDescription("Increase on category = Shoes Handbags & Sunglasses | city = Chicago contributes "
            + "the most to current incident.");
        return IncidentRootCauseTransforms.fromInner(innerRootCause);
    }

    void validateIncidentRootCauses(IncidentRootCause expectedIncidentRootCause,
        IncidentRootCause actualIncidentRootCause) {
        assertEquals(expectedIncidentRootCause.getSeriesKey(), actualIncidentRootCause.getSeriesKey());
        assertEquals(expectedIncidentRootCause.getDescription(), actualIncidentRootCause.getDescription());
        assertEquals(expectedIncidentRootCause.getPaths(), actualIncidentRootCause.getPaths());
        assertEquals(expectedIncidentRootCause.getConfidenceScore(), actualIncidentRootCause.getConfidenceScore());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }
}
