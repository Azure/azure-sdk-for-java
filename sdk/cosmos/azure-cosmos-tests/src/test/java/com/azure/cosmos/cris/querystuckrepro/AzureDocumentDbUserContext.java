package com.azure.cosmos.cris.querystuckrepro;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.util.CosmosPagedFlux;

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

    public void setContinuationToken(String token) {
        this.session.setContinuationToken(token);
    }

    public String getContinuationToken() {
        return this.session.getContinuationToken();
    }

    public void IncrementNoOfRecordRead() {
        this.session.IncrementNoOfRecordRead();
    }

    public long getNoOfRecordRead() {
        return this.session.getNoOfRecordRead();
    }

    public CosmosPagedFlux<Document> getCosmosPagedResponse() {
        return this.session.getCosmosPagedResponse();
    }

    public void setCosmosPagedResponse(CosmosPagedFlux<Document> value) {
        this.session.setCosmosPagedResponse(value);
    }
}
