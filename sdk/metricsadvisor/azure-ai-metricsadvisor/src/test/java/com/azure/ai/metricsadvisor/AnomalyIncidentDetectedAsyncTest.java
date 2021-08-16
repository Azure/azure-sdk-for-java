// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.test.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnomalyIncidentDetectedAsyncTest extends IncidentDetectedTestBase {

    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listIncidentsDetected(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAsyncClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();

        PagedFlux<AnomalyIncident> incidentsFlux
            = client.listIncidentsForDetectionConfig(
            ListIncidentsDetectedInput.INSTANCE.detectionConfigurationId,
            ListIncidentsDetectedInput.INSTANCE.startTime, ListIncidentsDetectedInput.INSTANCE.endTime,
            ListIncidentsDetectedInput.INSTANCE.options);

        Assertions.assertNotNull(incidentsFlux);
        final int[] i = {0};

        incidentsFlux.toIterable().forEach(incident -> {
            i[0]++;
            assertListIncidentsDetectedOutput(incident);
        });
        assertEquals(ListIncidentsDetectedOutput.INSTANCE.expectedIncidents, i[0]);
    }
}
