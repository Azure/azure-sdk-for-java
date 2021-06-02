// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;

@Fluent
public final class TransactionalBatchChangeSet extends MultipartPart<TransactionalBatchSubRequest> {
    public TransactionalBatchChangeSet() {
        super("changeset");
    }
}
