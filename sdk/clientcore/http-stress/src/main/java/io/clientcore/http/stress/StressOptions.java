// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.stress;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Options to be used by your stress tests.
*/
public class StressOptions extends PerfStressOptions {
    @Parameter(names = { "--endpoint" }, description = "Service endpoint")
    private String serviceEndpoint;

    /**
     * Gets the service endpoint.
     * @return the service endpoint.
     */
    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    // When adding new test parameters, consider adding them to TelemetryHelper.recordStart()
}
