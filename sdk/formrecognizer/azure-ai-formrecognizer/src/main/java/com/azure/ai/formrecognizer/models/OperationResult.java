// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.Objects;

public class OperationResult {
    /**
     * Identifier which contains the result of the model operation.
     */
    private final String resultId;


    public OperationResult(String resultId) {
        this.resultId = Objects.requireNonNull(resultId, "'modelId' cannot be null.");
    }

    /**
     * Get the modelId.
     *
     * @return the modelId
     */
    public String getResultId() {
        return this.resultId;
    }
}
