// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.template.stress;

import com.azure.perf.test.core.PerfStressTest;
import com.azure.sdk.template.stress.util.TelemetryHelper;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Performance test for getting messages.
 */
public abstract class ScenarioBase<TOptions extends StressOptions> extends PerfStressTest<TOptions> {
    private final TelemetryHelper telemetryHelper;
    private final Instant startTime = Instant.now();
    /**
     * Creates a stress test.
     *
     * @param options Performance test configuration options.
     * @param telemetryHelper Telemetry helper to monitor test execution and record stats.
     */
    public ScenarioBase(TOptions options, TelemetryHelper telemetryHelper) {
        super(options);
        this.telemetryHelper = telemetryHelper;
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        telemetryHelper.recordStart(options);
        return super.globalSetupAsync();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        telemetryHelper.recordEnd(startTime);
        return super.globalCleanupAsync();
    }
}
