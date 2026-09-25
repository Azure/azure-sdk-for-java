// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BufferedSourceTaskTest {
    @Test(groups = "unit", timeOut = 30_000)
    public void blockedReadStartsLazilyAndStopsCleanly() throws InterruptedException {
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
        assertThat(requestStarted.getCount()).isEqualTo(1);

        assertThat(task.poll()).isEmpty();
        assertThat(requestStarted.await(5, TimeUnit.SECONDS)).isTrue();

        task.stop();

        assertThat(requestExited.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(task.stopCount.get()).isEqualTo(1);
    }

    @Test(groups = "unit", timeOut = 30_000)
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

    private static final class TestTask extends BufferedSourceTask {
        private PollAction pollAction = Collections::emptyList;
        private Runnable stopAction = () -> { };
        private final AtomicInteger stopCount = new AtomicInteger();

        @Override
        public String version() {
            return "test";
        }

        @Override
        public void start(java.util.Map<String, String> props) {
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
        public void stop() {
            this.stopCount.incrementAndGet();
            this.stopAction.run();
            this.stopPolling();
        }
    }

    @FunctionalInterface
    private interface PollAction {
        List<SourceRecord> run() throws InterruptedException;
    }
}
