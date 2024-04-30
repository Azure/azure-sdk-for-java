package com.azure.cosmos.cris.querystuckrepro;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.FeedResponse;

import java.util.Iterator;
import java.util.Queue;

public class AzureDocumentDbUserContext {


    private final DataSession session;

    public AzureDocumentDbUserContext(DataSession session) {
        this.session = session;
    }

    public boolean isNoMoreRecords() {
        return this.session.isNoMoreRecords();
    }

    public void setNoMoreRecords(boolean value) {
        this.session.setNoMoreRecords(value);
    }

    public Queue<Object> getRecordQueue() {
        return this.session.getRecordQueue();
    }

    public void IncrementNoOfRecordRead() {
        this.session.IncrementNoOfRecordRead();
    }

    public long getNoOfRecordRead() {
        return this.session.getNoOfRecordRead();
    }

    public Iterator<FeedResponse<Document>> getCosmosPagedIterable() {
        return this.session.getCosmosPagedIterable();
    }

    public void setCosmosPagedIterable(Iterator<FeedResponse<Document>> value) {
        this.session.setCosmosPagedIterable(value);
    }
}
