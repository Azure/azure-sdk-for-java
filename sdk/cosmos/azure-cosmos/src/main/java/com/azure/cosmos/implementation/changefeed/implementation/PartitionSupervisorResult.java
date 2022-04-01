package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.CosmosException;

public class PartitionSupervisorResult {
    private final RuntimeException exception;

    PartitionSupervisorResult() {
        this.exception = null;
    }

    public PartitionSupervisorResult(RuntimeException cosmosException) {
        this.exception = cosmosException;
    }

    public RuntimeException getException() {
        return this.exception;
    }
}
