// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

public enum PatchOperationType {

    ADD(PatchConstants.OperationTypeNames_Add),
    REMOVE(PatchConstants.OperationTypeNames_Remove),
    REPLACE(PatchConstants.OperationTypeNames_Replace),
    SET(PatchConstants.OperationTypeNames_Set),
    INCREMENT(PatchConstants.OperationTypeNames_Increment);

    PatchOperationType(String operationValue) {
        this.operationValue = operationValue;
    }

    String getOperationValue() {
        return operationValue;
    }

    private final String operationValue;
}
