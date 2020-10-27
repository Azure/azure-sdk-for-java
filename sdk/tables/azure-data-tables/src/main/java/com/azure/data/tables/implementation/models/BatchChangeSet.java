package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpRequest;

@Fluent
public final class BatchChangeSet extends MultipartPart<HttpRequest> {
    public BatchChangeSet() {
        super("changeset");
    }
}
