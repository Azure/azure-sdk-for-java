// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.BatchRequestResponseConstant;

public enum CosmosItemOperationType {

    Create(BatchRequestResponseConstant.OPERATION_CREATE),
    Delete(BatchRequestResponseConstant.OPERATION_DELETE),
    Read(BatchRequestResponseConstant.OPERATION_READ),
    Replace(BatchRequestResponseConstant.OPERATION_REPLACE),
    Upsert(BatchRequestResponseConstant.OPERATION_UPSERT);

    CosmosItemOperationType(String operationValue) {
        this.operationValue = operationValue;
    }

    public String getOperationValue() {
        return operationValue;
    }

    private final String operationValue;
}
