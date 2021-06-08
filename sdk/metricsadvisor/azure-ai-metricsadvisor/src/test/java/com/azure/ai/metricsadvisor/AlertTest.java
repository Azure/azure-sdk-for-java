// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyAlert;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class AlertTest extends AlertTestBase {

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
    public void listAlerts(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();

        PagedIterable<AnomalyAlert> alertsIterable
            = client.listAlerts(ListAlertsInput.INSTANCE.alertConfigurationId, ListAlertsInput.INSTANCE.startTime,
            ListAlertsInput.INSTANCE.endTime, ListAlertsInput.INSTANCE.options, Context.NONE);

        int[] cnt = new int[1];
        for (AnomalyAlert alert : alertsIterable) {
            cnt[0]++;
            assertAlertOutput(alert);
        }
        Assertions.assertEquals(4, cnt[0]);
    }
}
