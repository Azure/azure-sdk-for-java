// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

@Fluent
public final class TransactionalBatchRequestBody extends MultipartPart<Object> {
    private final ClientLogger logger = new ClientLogger(TransactionalBatchRequestBody.class);
    private TransactionalBatchChangeSet changeSet = null;
    private boolean queryAdded = false;

    public TransactionalBatchRequestBody() {
        super("batch");
    }

    public TransactionalBatchRequestBody addQueryOperation(TransactionalBatchSubRequest queryRequest) {
        if (changeSet != null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Cannot add a query operation to a TransactionalBatchRequestBody containing a changeset."));
        }

        addContent(queryRequest);

        queryAdded = true;

        return this;
    }

    public TransactionalBatchRequestBody addChangeOperation(TransactionalBatchSubRequest changeRequest) {
        if (queryAdded) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Cannot add a change operation to a TransactionalBatchRequestBody containing query operations."));
        }

        if (changeSet == null) {
            changeSet = new TransactionalBatchChangeSet();

            addContent(changeSet);
        }

        changeSet.addContent(changeRequest);

        return this;
    }
}
