// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import java.util.Objects;

public class OperationContext {
    private final String identifier;
    private final int retryCount;

    private final String id;

    public OperationContext(
        String id,
        String identifier) {

        this(id, identifier, 0);
    }

    private OperationContext(
        String id,
        String identifier,
        int retryCount) {

        Objects.requireNonNull(id, "Argument 'doc' must have a non-null 'id' property.");
        Objects.requireNonNull(identifier, "Argument 'identifier' must not be null.");
        this.id = id;
        this.identifier = identifier;
        this.retryCount = retryCount;
    }

    public String getId() {
        return this.id;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public OperationContext createForRetry() {
        return new OperationContext(this.id, this.identifier, this.retryCount + 1);
    }
}
