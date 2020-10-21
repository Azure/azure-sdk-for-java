package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;

@Fluent
public final class BatchChangeSet extends MultipartPart<HttpOperation> {
    public BatchChangeSet() {
        super("changeset");
    }

    @Override
    public String toString() {
        return getContentType() + "\n\n" + super.toString();
    }
}
