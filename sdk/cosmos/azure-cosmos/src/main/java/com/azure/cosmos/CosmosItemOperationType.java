// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.util.Beta;

@Beta(Beta.SinceVersion.V4_7_0)
public enum CosmosItemOperationType {

    CREATE(BatchRequestResponseConstants.OPERATION_CREATE),
    DELETE(BatchRequestResponseConstants.OPERATION_DELETE),
    READ(BatchRequestResponseConstants.OPERATION_READ),
    REPLACE(BatchRequestResponseConstants.OPERATION_REPLACE),
    UPSERT(BatchRequestResponseConstants.OPERATION_UPSERT);

    CosmosItemOperationType(String operationValue) {
        this.operationValue = operationValue;
    }

    String getOperationValue() {
        return operationValue;
    }

    private final String operationValue;
}
