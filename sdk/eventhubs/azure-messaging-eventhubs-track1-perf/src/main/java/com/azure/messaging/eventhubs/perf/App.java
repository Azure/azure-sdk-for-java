// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.PerfStressProgram;

/**
 * Runs the Event Hubs performance tests.
 */
public class App {
    /**
     * Starts running a performance test.
     *
     * @param args Unused command line arguments.
     * @throws RuntimeException If not able to load test classes.
     */
    public static void main(String[] args) {
        final Class<?>[] testClasses = new Class<?>[]{
            EventProcessorTest.class,
            ReceiveEventsTest.class,
            SendEventDataTest.class,
            SendEventDataBatchTest.class,
            GetPartitionInformationTest.class,
        };

        PerfStressProgram.run(testClasses, args);
    }
}
