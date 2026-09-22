// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BufferedCosmosSourceTaskTest {
    private static final Duration TEST_POLL_WAIT = Duration.ofSeconds(2);
    private static final Duration TEST_SHUTDOWN_WAIT = Duration.ofSeconds(1);

    @Test(timeOut = 30_000)
    public void kafkaPollReturnsWhileCosmosRequestIsBlocked() throws InterruptedException {
        TestTask task = new TestTask();
        CountDownLatch requestStarted = new CountDownLatch(1);
        CountDownLatch releaseRequest = new CountDownLatch(1);
        task.pollAction = () -> {
            requestStarted.countDown();
            releaseRequest.await();
            return Collections.emptyList();
        };

        try {
            task.start(Collections.emptyMap());
            assertThat(requestStarted.await(5, TimeUnit.SECONDS)).isTrue();

            long startNanos = System.nanoTime();
            List<SourceRecord> records = task.poll();
            long durationMillis =
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

            assertThat(records).isEmpty();
            assertThat(durationMillis).isLessThan(3_000);
        } finally {
            releaseRequest.countDown();
            task.stop();
        }
    }

    @Test(timeOut = 30_000)
    public void stopClosesTaskAndUnblocksBackgroundRequest() throws InterruptedException {
        TestTask task = new TestTask();
        CountDownLatch requestStarted = new CountDownLatch(1);
        CountDownLatch requestExited = new CountDownLatch(1);
        CountDownLatch clientClosed = new CountDownLatch(1);
        task.pollAction = () -> {
            requestStarted.countDown();
            try {
                clientClosed.await();
                return Collections.emptyList();
            } finally {
                requestExited.countDown();
            }
        };
        task.stopAction = clientClosed::countDown;
        task.start(Collections.emptyMap());
        assertThat(requestStarted.await(5, TimeUnit.SECONDS)).isTrue();

        task.stop();

        assertThat(requestExited.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(task.stopCount.get()).isEqualTo(1);
    }

    @Test(timeOut = 30_000)
    public void readerRecoversAfterRetriableFailure() {
        TestTask task = new TestTask();
        RetriableException transientFailure =
            new RetriableException("temporarily unavailable");
        SourceRecord expected =
            new SourceRecord(Collections.emptyMap(), Collections.emptyMap(), "topic", null, null);
        AtomicInteger attempts = new AtomicInteger();
        task.pollAction = () -> {
            if (attempts.incrementAndGet() == 1) {
                throw transientFailure;
            }
            return Collections.singletonList(expected);
        };

        try {
            task.start(Collections.emptyMap());

            assertThatThrownBy(task::poll).isSameAs(transientFailure);
            assertThat(task.poll()).containsExactly(expected);
        } finally {
            task.stop();
        }
    }

    @Test(timeOut = 30_000)
    public void readerBuffersAtMostOneBatchAhead() throws InterruptedException {
        TestTask task = new TestTask();
        AtomicInteger pollCount = new AtomicInteger();
        CountDownLatch secondPollStarted = new CountDownLatch(1);
        CountDownLatch thirdPollStarted = new CountDownLatch(1);
        task.pollAction = () -> {
            int currentCount = pollCount.incrementAndGet();
            if (currentCount == 2) {
                secondPollStarted.countDown();
            } else if (currentCount == 3) {
                thirdPollStarted.countDown();
            }
            return Collections.singletonList(
                new SourceRecord(Collections.emptyMap(), Collections.emptyMap(), "topic", null, null));
        };

        try {
            task.start(Collections.emptyMap());
            assertThat(secondPollStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(pollCount.get()).isEqualTo(2);
            assertThat(thirdPollStarted.await(200, TimeUnit.MILLISECONDS)).isFalse();
        } finally {
            task.stop();
        }
    }

    @Test(timeOut = 30_000)
    public void startFailureStillStopsTask() {
        TestTask task = new TestTask();
        ConnectException expected = new ConnectException("start failed");
        task.startError = expected;

        assertThatThrownBy(() -> task.start(Collections.emptyMap())).isSameAs(expected);
        assertThat(task.stopCount.get()).isEqualTo(1);
    }

    @Test(timeOut = 30_000)
    public void readerFailureIsReportedToKafkaConnect() {
        TestTask task = new TestTask();
        AssertionError expected = new AssertionError("reader failed");
        task.pollAction = () -> {
            throw expected;
        };

        try {
            task.start(Collections.emptyMap());
            assertThatThrownBy(task::poll).isSameAs(expected);
        } finally {
            task.stop();
        }
    }

    @Test(timeOut = 30_000)
    public void taskCannotBeStartedTwice() {
        TestTask task = new TestTask();
        Map<String, String> configs = Collections.emptyMap();

        try {
            task.start(configs);
            assertThatThrownBy(() -> task.start(configs))
                .isInstanceOf(ConnectException.class)
                .hasMessageContaining("already running");
            assertThat(task.startCount.get()).isEqualTo(1);
        } finally {
            task.stop();
        }
    }

    private static final class TestTask extends BufferedCosmosSourceTask {
        private PollAction pollAction = Collections::emptyList;
        private Runnable stopAction = () -> { };
        private RuntimeException startError;
        private final AtomicInteger startCount = new AtomicInteger();
        private final AtomicInteger stopCount = new AtomicInteger();

        private TestTask() {
            super(TEST_POLL_WAIT, TEST_SHUTDOWN_WAIT);
        }

        @Override
        void startDelegate(Map<String, String> props) {
            this.startCount.incrementAndGet();
            if (this.startError != null) {
                throw this.startError;
            }
        }

        @Override
        List<SourceRecord> pollDelegate() {
            try {
                return this.pollAction.run();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            }
        }

        @Override
        void stopDelegate() {
            this.stopCount.incrementAndGet();
            this.stopAction.run();
        }
    }

    @FunctionalInterface
    private interface PollAction {
        List<SourceRecord> run() throws InterruptedException;
    }
}
