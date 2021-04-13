// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.perf;

import com.azure.ai.metricsadvisor.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * Performs root cause operations.
 */
public class RootCauseListTest extends ServiceTest<PerfStressOptions> {
    /**
     * Creates RootCauseTest object.
     *
     * @param options the configurable options for perf testing this class.
     */
    public RootCauseListTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        super.metricsAdvisorClient
            .listIncidentRootCauses(super.detectionConfigId,
                super.incidentId)
            .stream()
            .limit(super.maxListElements)
            .forEach(rootCause -> {
            });
    }

    @Override
    public Mono<Void> runAsync() {
        return super.metricsAdvisorAsyncClient
            .listIncidentRootCauses(super.detectionConfigId,
                super.incidentId)
            .take(super.maxListElements)
            .then();
    }
}
