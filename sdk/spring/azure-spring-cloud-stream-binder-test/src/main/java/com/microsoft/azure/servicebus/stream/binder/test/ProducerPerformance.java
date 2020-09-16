// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder.test;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.util.Random;

/**
 * Producer perf test via {@link MessageChannel}
 *
 * @author Warren Zhu
 */
public class ProducerPerformance {

    public static void startPerfTest(MessageChannel channel, int recordSize, int numRecords, int throughput) {

        byte[] payload = new byte[recordSize];
        Random random = new Random(0);

        for (int i = 0; i < payload.length; ++i) {
            payload[i] = (byte) (random.nextInt(26) + 65);
        }

        ProducerStatistics statistics = new ProducerStatistics(numRecords, 5000);
        long startMs = System.currentTimeMillis();

        ThroughputThrottler throttler = new ThroughputThrottler(throughput, startMs);

        int failedMessage = 0;

        for (int i = 0; i < numRecords; i++) {

            long sendStartMs = System.currentTimeMillis();

            boolean succeed = channel.send(new GenericMessage<>(payload));

            if (succeed) {
                long now = System.currentTimeMillis();
                statistics.record(payload.length, now - sendStartMs);
            } else {
                failedMessage++;
            }

            if (throttler.shouldThrottle(i, sendStartMs)) {
                throttler.throttle();
            }
        }

        System.out.println("Failed message count: " + failedMessage);
        statistics.printSummary();
    }

}
