// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverCloseReason;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.CheckpointFrequency;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.models.FeedResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class AutoCheckpointerTests {
    @Test(groups = "unit")
    public void defaultsCheckpointEveryBatch() {
        TestObserver observer = new TestObserver();
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(new CheckpointFrequency(), observer);
        TestContext context = new TestContext();

        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        checkpointer.closeAsync(context, ChangeFeedObserverCloseReason.SHUTDOWN).block();

        assertEquals(context.checkpoints, 2);
        assertEquals(context.shutdownCheckpoints, 0);
        assertEquals(observer.closes, 1);
    }

    @Test(groups = "unit")
    public void countsItemsAndResetsAfterCheckpoint() {
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(5), new TestObserver());
        TestContext context = new TestContext();

        checkpointer.processChanges(context, Collections.nCopies(3, "item")).block();
        assertEquals(context.checkpoints, 0);
        checkpointer.processChanges(context, Collections.nCopies(3, "item")).block();
        assertEquals(context.checkpoints, 1);
        checkpointer.processChanges(context, Collections.nCopies(3, "item")).block();
        assertEquals(context.checkpoints, 1);
    }

    @Test(groups = "unit")
    public void intervalOnlyAndIntervalReset() {
        TestClock clock = new TestClock();
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withTimeInterval(Duration.ofSeconds(30)), new TestObserver(), clock);
        TestContext context = new TestContext();

        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        assertEquals(context.checkpoints, 0);
        clock.advance(Duration.ofSeconds(30));
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        assertEquals(context.checkpoints, 1);
        clock.advance(Duration.ofSeconds(29));
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        assertEquals(context.checkpoints, 1);
        clock.advance(Duration.ofSeconds(1));
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        assertEquals(context.checkpoints, 2);
    }

    @Test(groups = "unit")
    public void eitherThresholdCanTriggerCheckpoint() {
        TestClock clock = new TestClock();
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(new CheckpointFrequency()
            .withTimeInterval(Duration.ofSeconds(30)).withProcessedDocumentCount(5), new TestObserver(), clock);
        TestContext context = new TestContext();

        checkpointer.processChanges(context, Collections.nCopies(5, "item")).block();
        assertEquals(context.checkpoints, 1);
        clock.advance(Duration.ofSeconds(30));
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        assertEquals(context.checkpoints, 2);
    }

    @Test(groups = "unit")
    public void waitsForSuccessfulHandlerCompletion() {
        TestObserver observer = new TestObserver();
        Sinks.One<Void> processing = Sinks.one();
        observer.processing = processing.asMono();
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(2), observer);
        TestContext context = new TestContext();

        Mono<Void> batch = checkpointer.processChanges(context, Collections.nCopies(2, "item"));
        assertEquals(observer.batches, 0);
        StepVerifier.create(batch)
            .then(() -> {
                assertEquals(observer.batches, 1);
                assertEquals(context.checkpoints, 0);
                processing.tryEmitEmpty();
            })
            .verifyComplete();
        assertEquals(context.checkpoints, 1);
    }

    @Test(groups = "unit")
    public void failedHandlerDoesNotCountOrReplacePendingProgress() {
        TestObserver observer = new TestObserver();
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(3), observer);
        TestContext successful = new TestContext();
        TestContext failed = new TestContext();
        checkpointer.processChanges(successful, Collections.singletonList("item")).block();

        observer.processing = Mono.error(new IllegalStateException("handler failed"));
        StepVerifier.create(checkpointer.processChanges(failed, Collections.nCopies(2, "item")))
            .expectErrorMessage("handler failed").verify();
        assertEquals(failed.checkpoints, 0);
        observer.processing = Mono.empty();
        checkpointer.processChanges(successful, Collections.singletonList("item")).block();
        assertEquals(successful.checkpoints, 0);
        checkpointer.closeAsync(failed, ChangeFeedObserverCloseReason.SHUTDOWN).block();
        assertEquals(successful.shutdownCheckpoints, 1);
        assertEquals(failed.shutdownCheckpoints, 0);
    }

    @Test(groups = "unit")
    public void failedCheckpointKeepsCountAndPendingProgress() {
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(2), new TestObserver());
        TestContext context = new TestContext();
        context.result = Mono.error(new IllegalStateException("checkpoint failed"));
        StepVerifier.create(checkpointer.processChanges(context, Collections.nCopies(2, "item")))
            .expectErrorMessage("checkpoint failed").verify();

        context.result = Mono.empty();
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        assertEquals(context.checkpoints, 2);
        checkpointer.closeAsync(context, ChangeFeedObserverCloseReason.SHUTDOWN).block();
        assertEquals(context.shutdownCheckpoints, 0);
    }

    @Test(groups = "unit")
    public void emptyResponseUsesPolicyWithoutCallingHandler() {
        TestClock clock = new TestClock();
        TestObserver observer = new TestObserver();
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withTimeInterval(Duration.ofSeconds(30)), observer, clock);
        TestContext first = new TestContext();
        TestContext latest = new TestContext();
        checkpointer.processChanges(first, Collections.singletonList("item")).block();
        checkpointer.processEmptyBatch(latest).block();
        assertEquals(latest.checkpoints, 0);
        clock.advance(Duration.ofSeconds(30));
        checkpointer.processEmptyBatch(latest).block();
        assertEquals(latest.checkpoints, 1);
        assertEquals(first.checkpoints, 0);
        assertEquals(observer.batches, 1);
    }

    @Test(groups = "unit")
    public void shutdownFlushesLatestEmptyContinuation() {
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(10), new TestObserver());
        TestContext first = new TestContext();
        TestContext latest = new TestContext();
        checkpointer.processChanges(first, Collections.singletonList("item")).block();
        checkpointer.processEmptyBatch(latest).block();
        checkpointer.closeAsync(first, ChangeFeedObserverCloseReason.SHUTDOWN).block();
        assertEquals(first.shutdownCheckpoints, 0);
        assertEquals(latest.shutdownCheckpoints, 1);
    }

    @DataProvider(name = "closeReasons")
    public Object[][] closeReasons() {
        ChangeFeedObserverCloseReason[] reasons = ChangeFeedObserverCloseReason.values();
        Object[][] data = new Object[reasons.length][1];
        for (int i = 0; i < reasons.length; i++) {
            data[i][0] = reasons[i];
        }
        return data;
    }

    @Test(groups = "unit", dataProvider = "closeReasons")
    public void flushesOnlyOnGracefulShutdown(ChangeFeedObserverCloseReason reason) {
        TestObserver observer = new TestObserver();
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(10), observer);
        TestContext context = new TestContext();
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        checkpointer.closeAsync(context, reason).block();
        assertEquals(context.shutdownCheckpoints, reason == ChangeFeedObserverCloseReason.SHUTDOWN ? 1 : 0);
        assertEquals(observer.closes, 1);
    }

    @Test(groups = "unit")
    public void failedShutdownCheckpointStillClosesObserver() {
        TestObserver observer = new TestObserver();
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(10), observer);
        TestContext context = new TestContext();
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        context.result = Mono.error(new IllegalStateException("lease lost"));
        StepVerifier.create(checkpointer.closeAsync(context, ChangeFeedObserverCloseReason.SHUTDOWN))
            .expectErrorMessage("lease lost").verify();
        assertEquals(observer.closes, 1);
    }

    @Test(groups = "unit")
    public void countsDeliveredItemsWhenHandlerClearsList() {
        TestObserver observer = new TestObserver();
        observer.clearDocs = true;
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(5), observer);
        TestContext context = new TestContext();
        List<String> first = new ArrayList<>(Collections.nCopies(3, "item"));
        checkpointer.processChanges(context, first).block();
        assertEquals(first.size(), 0);
        assertEquals(context.checkpoints, 0);
        checkpointer.processChanges(context, new ArrayList<>(Collections.nCopies(2, "item"))).block();
        assertEquals(context.checkpoints, 1);
    }

    @Test(groups = "unit")
    public void failedMutatingHandlerDoesNotCountDeliveredItems() {
        TestObserver observer = new TestObserver();
        observer.clearDocs = true;
        observer.processing = Mono.error(new IllegalStateException("handler failed"));
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(5), observer);
        TestContext context = new TestContext();
        StepVerifier.create(checkpointer.processChanges(context, new ArrayList<>(Collections.nCopies(3, "item"))))
            .expectErrorMessage("handler failed").verify();
        observer.processing = Mono.empty();
        checkpointer.processChanges(context, new ArrayList<>(Collections.nCopies(2, "item"))).block();
        assertEquals(context.checkpoints, 0);
        checkpointer.processChanges(context, new ArrayList<>(Collections.nCopies(3, "item"))).block();
        assertEquals(context.checkpoints, 1);
    }

    @DataProvider(name = "initialResponses")
    public Object[][] initialResponses() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = "unit", dataProvider = "initialResponses")
    public void initialContinuationIsPersistedBeforeDeferringCheckpoints(boolean empty) {
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(new CheckpointFrequency()
            .withProcessedDocumentCount(100).withTimeInterval(Duration.ofHours(1)), new TestObserver());
        TestContext context = new TestContext();
        context.initialCheckpointRequired = true;
        if (empty) {
            checkpointer.processEmptyBatch(context).block();
        } else {
            checkpointer.processChanges(context, Collections.singletonList("item")).block();
        }
        assertEquals(context.checkpoints, 1);
        context.initialCheckpointRequired = false;
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        assertEquals(context.checkpoints, 1);
    }

    @Test(groups = "unit")
    public void initialCheckpointRequiresSuccessfulProcessing() {
        TestObserver observer = new TestObserver();
        observer.processing = Mono.error(new IllegalStateException("handler failed"));
        AutoCheckpointer<String> checkpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(100), observer);
        TestContext context = new TestContext();
        context.initialCheckpointRequired = true;
        StepVerifier.create(checkpointer.processChanges(context, Collections.singletonList("item")))
            .expectErrorMessage("handler failed").verify();
        assertEquals(context.checkpoints, 0);
        observer.processing = Mono.empty();
        context.result = Mono.error(new IllegalStateException("checkpoint failed"));
        StepVerifier.create(checkpointer.processChanges(context, Collections.singletonList("item")))
            .expectErrorMessage("checkpoint failed").verify();
        assertEquals(context.checkpoints, 1);
        context.result = Mono.empty();
        checkpointer.processChanges(context, Collections.singletonList("item")).block();
        assertEquals(context.checkpoints, 2);
    }

    private static final class TestObserver implements ChangeFeedObserver<String> {
        private Mono<Void> processing = Mono.empty();
        private int batches;
        private boolean clearDocs;
        private int closes;

        @Override
        public void open(ChangeFeedObserverContext<String> context) {
        }

        @Override
        public void close(ChangeFeedObserverContext<String> context, ChangeFeedObserverCloseReason reason) {
            this.closes++;
        }

        @Override
        public Mono<Void> processChanges(ChangeFeedObserverContext<String> context, List<String> docs) {
            this.batches++;
            if (this.clearDocs) {
                docs.clear();
            }
            return this.processing;
        }
    }

    private static final class TestContext implements ChangeFeedObserverContext<String> {
        private int checkpoints;
        private boolean initialCheckpointRequired;
        private int shutdownCheckpoints;
        private Mono<Lease> result = Mono.empty();

        @Override
        public String getLeaseToken() {
            return "lease";
        }

        @Override
        public FeedResponse<String> getFeedResponse() {
            return null;
        }

        @Override
        public boolean isInitialCheckpointRequired() {
            return this.initialCheckpointRequired;
        }

        @Override
        public Mono<Lease> checkpoint() {
            this.checkpoints++;
            return this.result;
        }

        @Override
        public Mono<Lease> checkpointOnShutdown() {
            this.shutdownCheckpoints++;
            return this.result;
        }
    }

    private static final class TestClock extends Clock {
        private Instant now = Instant.EPOCH;

        private void advance(Duration duration) {
            this.now = this.now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return this.now;
        }
    }
}
