// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.util.Beta;

/**
 * @deprecated forRemoval = true, since = "4.19"
 * This class is not necessary anymore and will be removed. Please use {@link com.azure.cosmos.models.CosmosItemOperationType}
 */
@Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
@Deprecated() //forRemoval = true, since = "4.19"
public enum CosmosItemOperationType {

    @Deprecated() //forRemoval = true, since = "4.19"
    CREATE(BatchRequestResponseConstants.OPERATION_CREATE),

    @Deprecated() //forRemoval = true, since = "4.19"
    DELETE(BatchRequestResponseConstants.OPERATION_DELETE),

    @Deprecated() //forRemoval = true, since = "4.19"
    READ(BatchRequestResponseConstants.OPERATION_READ),

    @Deprecated() //forRemoval = true, since = "4.19"
    REPLACE(BatchRequestResponseConstants.OPERATION_REPLACE),

    @Deprecated() //forRemoval = true, since = "4.19"
    UPSERT(BatchRequestResponseConstants.OPERATION_UPSERT),

    @Deprecated() //forRemoval = true, since = "4.19"
    PATCH(BatchRequestResponseConstants.OPERATION_PATCH);

    CosmosItemOperationType(String operationValue) {
        this.operationValue = operationValue;
    }

    String getOperationValue() {
        return operationValue;
    }

    private final String operationValue;
}
