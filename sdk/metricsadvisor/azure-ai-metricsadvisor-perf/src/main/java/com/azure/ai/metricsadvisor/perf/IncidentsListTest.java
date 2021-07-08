// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.perf;

import com.azure.ai.metricsadvisor.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * Performs incidents list operations.
 */
public class IncidentsListTest extends ServiceTest<PerfStressOptions> {
    /**
     * Creates IncidentsListTest object.
     *
     * @param options the configurable options for perf testing this class.
     */
    public IncidentsListTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        super.metricsAdvisorClient
            .listIncidentsForAlert(super.alertConfigId,
                super.alertId)
            .stream()
            .limit(super.maxListElements)
            .forEach(incident -> {
            });
    }

    @Override
    public Mono<Void> runAsync() {
        return super.metricsAdvisorAsyncClient
            .listIncidentsForAlert(super.alertConfigId,
                super.alertId)
            .take(super.maxListElements)
            .then();
    }
}
