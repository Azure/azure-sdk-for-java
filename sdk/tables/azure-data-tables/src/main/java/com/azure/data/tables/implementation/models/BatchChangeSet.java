package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;

@Fluent
public final class BatchChangeSet extends MultipartPart<BatchSubRequest> {
    public BatchChangeSet() {
        super("changeset");
    }
}
