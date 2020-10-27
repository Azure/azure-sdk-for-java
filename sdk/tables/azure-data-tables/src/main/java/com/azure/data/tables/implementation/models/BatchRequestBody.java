package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpRequest;

@Fluent
public final class BatchRequestBody extends MultipartPart<Object> {
    private BatchChangeSet changeSet = null;
    private boolean queryAdded = false;

    public BatchRequestBody() {
        super("batch");
    }

    public BatchRequestBody addQueryOperation(HttpRequest queryRequest) {
        if (changeSet != null) {
            throw new IllegalStateException("Cannot add a query operation to a BatchRequestBody containing a changeset.");
        }
        addContent(queryRequest);
        queryAdded = true;
        return this;
    }

    public BatchRequestBody addChangeOperation(HttpRequest changeRequest) {
        if (queryAdded) {
            throw new IllegalStateException("Cannot add a change operation to a BatchRequestBody containing query operations.");
        }
        if (changeSet == null) {
            changeSet = new BatchChangeSet();
            addContent(changeSet);
        }
        changeSet.addContent(changeRequest);
        return this;
    }
}
