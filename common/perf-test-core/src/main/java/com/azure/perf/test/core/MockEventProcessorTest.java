// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.Parameter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Represents a Mock Event Processor Test.
 */
public class MockEventProcessorTest extends EventPerfTest<MockEventProcessorTest.MockEventProcessorPerfOptions> {
    private final MockEventProcessor mockEventProcessor;

    /**
     * Creates an instance of Mock Event Processor Test
     * @param perfStressOptions the options to used to configure the test.
     */
    public MockEventProcessorTest(MockEventProcessorPerfOptions perfStressOptions) {
        super(perfStressOptions);
        Consumer<MockErrorContext> errorProcessor = mockErrorContext -> errorRaised(mockErrorContext.getThrowable());

        Consumer<MockEventContext> eventProcessor = mockEventContext -> eventRaised();

        Duration errorAfter = perfStressOptions.getErrorAfterInSeconds() > 0
            ? Duration.ofSeconds(perfStressOptions.getErrorAfterInSeconds()) : null;

        mockEventProcessor = new MockEventProcessor(perfStressOptions.getPartitions(), perfStressOptions.getMaxEventsPerSecond(), errorAfter,
            errorProcessor, eventProcessor);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.defer(() -> {
            mockEventProcessor.start();
            return Mono.empty();
        }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.defer(() -> {
            mockEventProcessor.stop();
            return Mono.empty();
        }).then(super.cleanupAsync());
    }

    /**
     * Represents the perf options for Mock Event Processor Test.
     */
    public static class MockEventProcessorPerfOptions extends PerfStressOptions {
        @Parameter(names = { "-meps", "--maxEventsPerSecond" }, description = "Maximum Events to send per second.")
        private int maxEventsPerSecond = 0;


        @Parameter(names = { "-ea", "--errorAfter" }, description = "Error After duration in seconds.")
        private int errorAfterInSeconds = 0;

        @Parameter(names = { "-pt", "--partitions" }, description = "Number of Partitions.")
        private int partitions = 1;


        /**
         * Get Error after duration in seconds.
         * @return the error after duration in seconds.
         */
        public int getErrorAfterInSeconds() {
            return errorAfterInSeconds;
        }

        /**
         * Get Maximum events per second.
         * @return the max events per second.
         */
        public int getMaxEventsPerSecond() {
            return maxEventsPerSecond;
        }

        /**
         * Get Maximum events per second.
         * @return the max events per second.
         */
        public int getPartitions() {
            return partitions;
        }
    }
}
