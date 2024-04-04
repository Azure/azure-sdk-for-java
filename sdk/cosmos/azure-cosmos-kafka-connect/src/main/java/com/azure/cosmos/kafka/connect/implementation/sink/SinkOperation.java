// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import org.apache.kafka.connect.sink.SinkRecord;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SinkOperation {
    private final SinkRecord sinkRecord;
    private final AtomicInteger retryCount;
    private final AtomicReference<Throwable> exception;
    private final AtomicBoolean completed;

    public SinkOperation(SinkRecord sinkRecord) {
        this.sinkRecord = sinkRecord;
        this.retryCount = new AtomicInteger(0);
        this.exception = new AtomicReference<>(null);
        this.completed = new AtomicBoolean(false);
    }

    public SinkRecord getSinkRecord() {
        return this.sinkRecord;
    }

    public long getKafkaOffset() {
        return this.sinkRecord.kafkaOffset();
    }

    public Integer getKafkaPartition() {
        return this.sinkRecord.kafkaPartition();
    }

    public String getTopic() {
        return this.sinkRecord.topic();
    }

    public int getRetryCount() {
        return this.retryCount.get();
    }

    public void retry() {
        this.retryCount.incrementAndGet();
    }

    public Throwable getException() {
        return this.exception.get();
    }

    public void setException(Throwable exception) {
        this.exception.set(exception);
    }

    public boolean isCompleted() {
        return this.completed.get();
    }

    public void complete() {
        this.completed.set(true);
    }
}
