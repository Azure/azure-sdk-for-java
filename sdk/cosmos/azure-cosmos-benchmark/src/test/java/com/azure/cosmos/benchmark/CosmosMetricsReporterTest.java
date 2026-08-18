// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosMetricName;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosMetricsReporterTest {

    private static final String PPCB_FAILBACK_METRIC =
        CosmosMetricName.PPCB_FAILBACK_PENDING_RECOVERY_COUNT.toString();

    @Test(groups = {"unit"})
    public void reportsZeroForPpcbFailbackGauge() {
        assertThat(CosmosMetricsReporter.shouldReportGauge(PPCB_FAILBACK_METRIC, 0)).isTrue();
    }

    @Test(groups = {"unit"})
    public void skipsZeroForUnrelatedGauge() {
        assertThat(CosmosMetricsReporter.shouldReportGauge("cosmos.client.other", 0)).isFalse();
    }

    @Test(groups = {"unit"})
    public void skipsNaNForPpcbFailbackGauge() {
        assertThat(CosmosMetricsReporter.shouldReportGauge(PPCB_FAILBACK_METRIC, Double.NaN)).isFalse();
    }

    @Test(groups = {"unit"})
    public void reportsNonZeroForUnrelatedGauge() {
        assertThat(CosmosMetricsReporter.shouldReportGauge("cosmos.client.other", 1)).isTrue();
    }
}
