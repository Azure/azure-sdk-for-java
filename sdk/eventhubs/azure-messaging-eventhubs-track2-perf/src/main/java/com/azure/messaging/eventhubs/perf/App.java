// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.perf.core.EventProcessorBatchStorageTest;
import com.azure.messaging.eventhubs.perf.core.EventProcessorStorageTest;
import com.azure.messaging.eventhubs.perf.core.ReceiveEventsBatchTest;
import com.azure.messaging.eventhubs.perf.core.ReceiveEventsFromPartitionBatchTest;
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
            SendEventBatchTest.class,
            ReceiveEventsFromPartitionBatchTest.class,
            ReceiveEventsBatchTest.class,
            EventProcessorStorageTest.class,
            EventProcessorBatchStorageTest.class,
            ReceiveEventsTest.class,
            SendEventDataTest.class,
            SendEventDataBatchTest.class,
            EventProcessorTest.class,
            GetPartitionInformationTest.class,
            ReactorReceiveEventsTest.class,
            EventProcessorJedisTest.class
        };
        PerfStressProgram.run(testClasses, args);
    }
}
