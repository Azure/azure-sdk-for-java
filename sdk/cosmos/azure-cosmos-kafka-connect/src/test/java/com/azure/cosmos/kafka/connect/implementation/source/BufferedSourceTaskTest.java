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

public class BufferedSourceTaskTest {
    private static final Duration TEST_POLL_WAIT = Duration.ofSeconds(2);
    private static final Duration TEST_SHUTDOWN_WAIT = Duration.ofSeconds(1);

    @Test(timeOut = 30_000)
    public void kafkaPollReturnsWhileSourceRequestIsBlocked() throws InterruptedException {
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
            assertThat(task.poll()).isEmpty();
        } finally {
            releaseRequest.countDown();
            task.stop();
        }
    }

    @Test(timeOut = 30_000)
    public void sourceReaderStartsLazilyOnFirstPoll() {
        TestTask task = new TestTask();
        AtomicInteger pollCount = new AtomicInteger();
        task.pollAction = () -> {
            pollCount.incrementAndGet();
            return Collections.emptyList();
        };

        try {
            task.start(Collections.emptyMap());
            assertThat(pollCount.get()).isZero();

            task.poll();

            assertThat(pollCount.get()).isGreaterThanOrEqualTo(1);
        } finally {
            task.stop();
        }
    }

    @Test(timeOut = 30_000)
    public void stopClosesTaskAndUnblocksBackgroundRequest() throws InterruptedException {
        TestTask task = new TestTask();
        CountDownLatch requestStarted = new CountDownLatch(1);
        CountDownLatch requestExited = new CountDownLatch(1);
        CountDownLatch taskStopped = new CountDownLatch(1);
        task.pollAction = () -> {
            requestStarted.countDown();
            try {
                taskStopped.await();
                return Collections.emptyList();
            } finally {
                requestExited.countDown();
            }
        };
        task.stopAction = taskStopped::countDown;
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
    public void startFailureStillStopsTask() {
        TestTask task = new TestTask();
        ConnectException expected = new ConnectException("start failed");
        task.startError = expected;

        assertThatThrownBy(() -> task.start(Collections.emptyMap())).isSameAs(expected);
        assertThat(task.stopCount.get()).isEqualTo(1);
    }

    private static final class TestTask extends BufferedSourceTask {
        private PollAction pollAction = Collections::emptyList;
        private Runnable stopAction = () -> { };
        private RuntimeException startError;
        private final AtomicInteger stopCount = new AtomicInteger();

        private TestTask() {
            super(TEST_POLL_WAIT, TEST_SHUTDOWN_WAIT);
        }

        @Override
        public String version() {
            return "test";
        }

        @Override
        protected void startTask(Map<String, String> props) {
            if (this.startError != null) {
                throw this.startError;
            }
        }

        @Override
        protected List<SourceRecord> pollTask() {
            try {
                return this.pollAction.run();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            }
        }

        @Override
        protected void stopTask() {
            this.stopCount.incrementAndGet();
            this.stopAction.run();
        }
    }

    @FunctionalInterface
    private interface PollAction {
        List<SourceRecord> run() throws InterruptedException;
    }
}
