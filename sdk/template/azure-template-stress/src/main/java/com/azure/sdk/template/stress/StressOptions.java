package com.azure.sdk.template.stress;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Options to be used by your stress tests.
*/
public class StressOptions extends PerfStressOptions {
    @Parameter(names = { "--endpoint" }, description = "Service endpoint")
    private String serviceEndpoint;

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    // When adding new test parameters, consider adding them to TelemetryHelper.recordStart()
}
