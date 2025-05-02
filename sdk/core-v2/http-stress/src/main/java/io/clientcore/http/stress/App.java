// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.stress;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.core.http.stress.util.TelemetryHelper;

/**
 * Stress test application
 */
public final class App {
    /**
     * Main method to invoke other stress tests.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        TelemetryHelper.init();

        PerfStressProgram.run(new Class<?>[] { HttpGet.class,
            // HttpPatch.class,
            // add other stress tests here
        }, args);
    }

    private App() {
    }
}
