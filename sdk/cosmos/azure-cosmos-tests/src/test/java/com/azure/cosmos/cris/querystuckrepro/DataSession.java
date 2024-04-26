package com.azure.cosmos.cris.querystuckrepro;

import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedFlux;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class DataSession {

    private String continuationToken;
    private Queue<Object> recordQueue = new LinkedBlockingQueue<>();

    private AtomicLong numberOfRecordsRead = new AtomicLong(0);

    private boolean isNoMoreRecords = false;

    private CosmosPagedFlux<Document> pagedResponse = null;

    public DataSession() {

    }

    public String getContinuationToken() {
        return this.continuationToken;
    }

    public void setContinuationToken(String token) {
        this.continuationToken = token;
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

    public CosmosPagedFlux<Document> getCosmosPagedResponse() {
        return this.pagedResponse;
    }

    public void setCosmosPagedResponse(CosmosPagedFlux<Document> value) {
        this.pagedResponse= value;
    }
}
