// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.util.IterableStream;
import com.beust.jcommander.Parameter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Test class for Mock Batch Receiver.
 */
public class MockBatchReceiverTest extends BatchPerfTest<MockBatchReceiverTest.MockReceiverOptions> {
    final MockReceiver mockReceiver;
    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public MockBatchReceiverTest(MockReceiverOptions options) {
        super(options);
        mockReceiver = new MockReceiver();
    }

    @Override
    CompletableFuture<Integer> runTestAsyncWithCompletableFuture() {
        return null;
    }

    @Override
    Runnable runTestAsyncWithExecutorService() {
        return null;
    }

    @Override
    void runTestAsyncWithVirtualThread() {

    }

    @Override
    public int runBatch() {
        return ((Long) mockReceiver.receive(options.minMessageCount, options.maxMessageCount).stream().count())
            .intValue();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        return mockReceiver.receiveAsync(options.minMessageCount, options.maxMessageCount)
            .count().map(count -> count.intValue());
    }

    /**
     * Options class for Mock Receiver Test.
     */
    public static class MockReceiverOptions extends PerfStressOptions {
        @Parameter(names = {"--max-message-count" }, description = "Max messages to Receive")
        private int maxMessageCount = 10;

        @Parameter(names = {"--min-message-count" }, description = "Min messages to Receive")
        private int minMessageCount = 0;

        /**
         * Get Max message count;
         * @return the max message count.
         */
        public int getMaxMessageCount() {
            return maxMessageCount;
        }

        /**
         * Get the Min Message count.
         * @return the Min
         */
        public int getMinMessageCount() {
            return minMessageCount;
        }
    }

    private static class MockReceiver {
        public IterableStream<Integer> receive(int minMessageCount, int maxMessageCount) {
            int returnedMessages = (int) ((Math.random() * (maxMessageCount - minMessageCount)) + minMessageCount);

            return IterableStream.of(IntStream.range(1, returnedMessages).boxed().collect(Collectors.toList()));
        }

        public  Flux<Integer> receiveAsync(int minMessageCount, int maxMessageCount) {
            return Flux.fromIterable(receive(minMessageCount, maxMessageCount));
        }
    }
}
