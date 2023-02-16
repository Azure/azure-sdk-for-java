// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.OperationResultHelper;
import com.azure.core.annotation.Immutable;

/**
 * The OperationResult model.
 */
@Immutable
public final class OperationResult {
    /**
     * Identifier which contains the result of the build model/analyze operation.
     */
    private String operationId;

    /**
     * Gets an ID representing the operation that can be used to poll for the status
     * of the long-running operation.
     *
     * @return the operationId.
     */
    public String getOperationId() {
        return this.operationId;
    }

    private void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    static {
        OperationResultHelper.setAccessor(
            new OperationResultHelper.OperationResultAccessor() {
                @Override
                public void setOperationId(
                    OperationResult operationResult, String operationId) {
                    operationResult.setOperationId(operationId);
                }
            });
    }
}
