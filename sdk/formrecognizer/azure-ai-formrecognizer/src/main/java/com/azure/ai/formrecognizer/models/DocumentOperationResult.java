// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentOperationResultHelper;

/**
 * The DocumentOperationResult model.
 */
public final class DocumentOperationResult {
    /**
     * Identifier which contains the result of the model/analyze operation.
     */
    private String resultId;

    /**
     * Gets an ID representing the operation that can be used to poll for the status
     * of the long-running operation.
     *
     * @return the resultId.
     */
    public String getResultId() {
        return this.resultId;
    }

    void setResultId(String resultId) {
        this.resultId = resultId;
    }

    static {
        DocumentOperationResultHelper.setAccessor(
            new DocumentOperationResultHelper.DocumentOperationResultAccessor() {
                @Override
                public void setResultId(
                    DocumentOperationResult documentOperationResult, String resultId) {
                    documentOperationResult.setResultId(resultId);
                }
            });
    }
}
