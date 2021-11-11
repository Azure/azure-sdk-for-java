// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;

/**
 * Depicts type of Cosmos Item Operation
 */
public enum CosmosItemOperationType {

    CREATE(BatchRequestResponseConstants.OPERATION_CREATE),

    DELETE(BatchRequestResponseConstants.OPERATION_DELETE),

    READ(BatchRequestResponseConstants.OPERATION_READ),

    REPLACE(BatchRequestResponseConstants.OPERATION_REPLACE),

    UPSERT(BatchRequestResponseConstants.OPERATION_UPSERT),

    PATCH(BatchRequestResponseConstants.OPERATION_PATCH);

    CosmosItemOperationType(String operationValue) {
        this.operationValue = operationValue;
    }

    String getOperationValue() {
        return operationValue;
    }

    private final String operationValue;
}
