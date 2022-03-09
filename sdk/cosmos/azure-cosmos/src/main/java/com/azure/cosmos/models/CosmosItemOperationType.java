// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;

/**
 * Depicts type of Cosmos Item Operation
 */
public enum CosmosItemOperationType {

    /**
     * Create operation type.
     */
    CREATE(BatchRequestResponseConstants.OPERATION_CREATE),

    /**
     * Delete operation type.
     */
    DELETE(BatchRequestResponseConstants.OPERATION_DELETE),

    /**
     * Read operation type.
     */
    READ(BatchRequestResponseConstants.OPERATION_READ),

    /**
     * Replace operation type.
     */
    REPLACE(BatchRequestResponseConstants.OPERATION_REPLACE),

    /**
     * Upsert operation type.
     */
    UPSERT(BatchRequestResponseConstants.OPERATION_UPSERT),

    /**
     * Patch operation type.
     */
    PATCH(BatchRequestResponseConstants.OPERATION_PATCH);

    CosmosItemOperationType(String operationValue) {
        this.operationValue = operationValue;
    }

    String getOperationValue() {
        return operationValue;
    }

    private final String operationValue;
}
