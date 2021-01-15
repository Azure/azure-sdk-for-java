// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

@Fluent
public final class BatchRequestBody extends MultipartPart<Object> {
    private final ClientLogger logger = new ClientLogger(BatchRequestBody.class);
    private BatchChangeSet changeSet = null;
    private boolean queryAdded = false;

    public BatchRequestBody() {
        super("batch");
    }

    public BatchRequestBody addQueryOperation(BatchSubRequest queryRequest) {
        if (changeSet != null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Cannot add a query operation to a BatchRequestBody containing a changeset."));
        }
        addContent(queryRequest);
        queryAdded = true;
        return this;
    }

    public BatchRequestBody addChangeOperation(BatchSubRequest changeRequest) {
        if (queryAdded) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Cannot add a change operation to a BatchRequestBody containing query operations."));
        }
        if (changeSet == null) {
            changeSet = new BatchChangeSet();
            addContent(changeSet);
        }
        changeSet.addContent(changeRequest);
        return this;
    }
}
