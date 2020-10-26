// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.BatchRequestResponseConstant;
import com.azure.cosmos.util.Beta;

@Beta(Beta.SinceVersion.V4_7_0)
public enum CosmosItemOperationType {

    CREATE(BatchRequestResponseConstant.OPERATION_CREATE),
    DELETE(BatchRequestResponseConstant.OPERATION_DELETE),
    READ(BatchRequestResponseConstant.OPERATION_READ),
    REPLACE(BatchRequestResponseConstant.OPERATION_REPLACE),
    UPSERT(BatchRequestResponseConstant.OPERATION_UPSERT);

    CosmosItemOperationType(String operationValue) {
        this.operationValue = operationValue;
    }

    String getOperationValue() {
        return operationValue;
    }

    private final String operationValue;
}
