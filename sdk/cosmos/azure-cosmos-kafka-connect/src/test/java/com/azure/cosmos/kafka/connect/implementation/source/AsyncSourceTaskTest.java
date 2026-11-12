// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AsyncSourceTaskTest {
    @Test(groups = "unit", timeOut = 30_000)
    public void blockedReadStartsLazilyAndStopsCleanly() throws InterruptedException {
        TestTask task = new TestTask();
        CountDownLatch requestStarted = new CountDownLatch(1);
        CountDownLatch requestCancelled = new CountDownLatch(1);
        task.pollAction = () -> Mono.<List<SourceRecord>>never()
            .doOnSubscribe(ignored -> requestStarted.countDown())
            .doOnCancel(requestCancelled::countDown);
        task.start(Collections.emptyMap());
        assertThat(requestStarted.getCount()).isEqualTo(1);

        assertThat(task.poll()).isEmpty();
        assertThat(requestStarted.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(task.poll()).isEmpty();
        assertThat(task.pollCount.get()).isEqualTo(1);

        task.stop();

        assertThat(requestCancelled.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(task.stopCount.get()).isEqualTo(1);
    }

    @Test(groups = "unit", timeOut = 30_000)
    public void pollRecoversAfterRetriableFailure() {
        TestTask task = new TestTask();
        RetriableException transientFailure =
            new RetriableException("temporarily unavailable");
        SourceRecord expected = org.mockito.Mockito.mock(SourceRecord.class);
        AtomicInteger attempts = new AtomicInteger();
        task.pollAction = () -> Mono.defer(() -> {
            if (attempts.incrementAndGet() == 1) {
                return Mono.error(transientFailure);
            }
            return Mono.just(Collections.singletonList(expected));
        });

        try {
            task.start(Collections.emptyMap());
            assertThatThrownBy(task::poll).isSameAs(transientFailure);
            List<SourceRecord> records;
            do {
                records = task.poll();
            } while (records.isEmpty());
            assertThat(records).containsExactly(expected);
        } finally {
            task.stop();
        }
    }

    @Test(groups = "unit", timeOut = 30_000)
    public void pollDoesNotRepeatAfterNonRetriableFailure() throws InterruptedException {
        TestTask task = new TestTask();
        AtomicInteger attempts = new AtomicInteger();
        RuntimeException terminalFailure = new RuntimeException("terminal");
        task.pollAction = () -> Mono.defer(() -> {
            attempts.incrementAndGet();
            return Mono.error(terminalFailure);
        });

        try {
            task.start(Collections.emptyMap());
            assertThatThrownBy(task::poll).isSameAs(terminalFailure);
            Thread.sleep(200);
            assertThat(attempts.get()).isEqualTo(1);
        } finally {
            task.stop();
        }
    }

    @Test(groups = "unit", timeOut = 30_000)
    public void stopCancelsInFlightPoll() throws InterruptedException {
        TestTask task = new TestTask();
        CountDownLatch requestStarted = new CountDownLatch(1);
        CountDownLatch requestCancelled = new CountDownLatch(1);
        task.pollAction = () -> Mono.<List<SourceRecord>>never()
            .doOnSubscribe(ignored -> requestStarted.countDown())
            .doOnCancel(requestCancelled::countDown);

        task.start(Collections.emptyMap());
        assertThat(task.poll()).isEmpty();
        assertThat(requestStarted.await(5, TimeUnit.SECONDS)).isTrue();

        task.stop();

        assertThat(requestCancelled.await(1, TimeUnit.SECONDS)).isTrue();
    }

    private static final class TestTask extends AsyncSourceTask {
        private PollAction pollAction = () -> Mono.just(Collections.emptyList());
        private Runnable stopAction = () -> { };
        private final AtomicInteger pollCount = new AtomicInteger();
        private final AtomicInteger stopCount = new AtomicInteger();

        @Override
        public String version() {
            return "test";
        }

        @Override
        public void start(java.util.Map<String, String> props) {
        }

        @Override
        protected Mono<List<SourceRecord>> pollTask() {
            this.pollCount.incrementAndGet();
            return this.pollAction.run();
        }

        @Override
        protected void stopTask() {
            this.stopCount.incrementAndGet();
            this.stopAction.run();
        }
    }

    @FunctionalInterface
    private interface PollAction {
        Mono<List<SourceRecord>> run();
    }
}
