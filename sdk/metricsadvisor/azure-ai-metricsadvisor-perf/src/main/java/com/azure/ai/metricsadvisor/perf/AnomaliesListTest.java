// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.perf;

import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.perf.core.ServiceTest;
import com.azure.core.util.Context;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * Performs anomalies list operations.
 */
public class AnomaliesListTest extends ServiceTest<PerfStressOptions> {
    /**
     * Creates AnomaliesListTest object.
     *
     * @param options the configurable options for perf testing this class.
     */
    public AnomaliesListTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        super.metricsAdvisorClient
            .listAnomaliesForAlert(super.alertConfigId,
                super.alertId,
                new ListAnomaliesAlertedOptions(),
                Context.NONE)
            .stream()
            .limit(super.maxListElements)
            .forEach(anomaly -> {
            });
    }

    @Override
    public Mono<Void> runAsync() {
        return super.metricsAdvisorAsyncClient
            .listAnomaliesForAlert(super.alertConfigId,
                super.alertId,
                new ListAnomaliesAlertedOptions())
            .take(super.maxListElements)
            .then();
    }
}
