package com.azure.cosmos.kafka.connect.implementation.sink;

import org.apache.kafka.connect.sink.SinkRecord;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SinkOperationContext {
    private final SinkRecord sinkRecord;
    private final AtomicInteger retryCount;
    private final AtomicBoolean isSucceeded;
    private final AtomicReference<Throwable> exception;
    private final AtomicBoolean completed;

    public SinkOperationContext(SinkRecord sinkRecord) {
        this.sinkRecord = sinkRecord;
        this.retryCount = new AtomicInteger(0);
        this.isSucceeded = new AtomicBoolean(false);
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

    public boolean getIsSucceeded() {
        return isSucceeded.get();
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    public Throwable getException() {
        return this.exception.get();
    }

    public void setException(Throwable exception) {
        this.exception.set(exception);
    }

    public void setSucceeded() {
        this.isSucceeded.set(true);
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public void complete() {
        completed.set(true);
    }
}
