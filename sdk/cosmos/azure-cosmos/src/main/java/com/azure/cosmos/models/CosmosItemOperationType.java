// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.util.Beta;

/**
 * Depicts type of Cosmos Item Operation
 */
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
