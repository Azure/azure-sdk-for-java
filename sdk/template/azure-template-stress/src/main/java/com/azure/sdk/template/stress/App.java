// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.template.stress;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Stress test application
 */
public class App {

    /**
     * Main method to invoke other stress tests.
     * @param args the input arguments
     */
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            HttpGet.class,
            // add other stress tests here
        }, args);
    }
}
