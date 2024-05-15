package com.azure.cosmos.cris.querystuckrepro;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.FeedResponse;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class DataSession {

    private final Queue<Object> recordQueue = new LinkedBlockingQueue<>();

    private final AtomicLong numberOfRecordsRead = new AtomicLong(0);

    private boolean isNoMoreRecords = false;

    private Iterator<FeedResponse<Document>> pagedIterable = null;

    public DataSession() {

    }

    public Queue<Object> getRecordQueue() {
        return this.recordQueue;
    }

    public boolean isNoMoreRecords() {
        return this.isNoMoreRecords;
    }

    public void setNoMoreRecords(boolean value) {
        this.isNoMoreRecords = value;
    }

    public void IncrementNoOfRecordRead() {
        this.numberOfRecordsRead.incrementAndGet();
    }

    public long getNoOfRecordRead() {
        return this.numberOfRecordsRead.get();
    }

    public Iterator<FeedResponse<Document>> getCosmosPagedIterable() {
        return this.pagedIterable;
    }

    public void setCosmosPagedIterable(Iterator<FeedResponse<Document>> value) {
        this.pagedIterable= value;
    }
}
