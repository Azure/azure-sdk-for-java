// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.OperationType;

import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstant.OPERATION_CREATE;
import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstant.OPERATION_DELETE;
import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstant.OPERATION_READ;
import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstant.OPERATION_REPLACE;
import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstant.OPERATION_UPSERT;

/**
 * Util methods for batch requests/response.
 */
class BatchExecUtils {

    static String getStringOperationType(OperationType operationType) {
        switch (operationType) {
            case Create:
                return OPERATION_CREATE;
            case Delete:
                return OPERATION_DELETE;
            case Read:
                return OPERATION_READ;
            case Replace:
                return OPERATION_REPLACE;
            case Upsert:
                return OPERATION_UPSERT;
        }

        return null;
    }
}
