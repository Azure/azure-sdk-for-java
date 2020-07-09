// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Objects;

/**
 * The OperationResult model.
 */
@Immutable
public final class OperationResult {
    /**
     * Identifier which contains the result of the model/analyze operation.
     */
    private final String resultId;

    /**
     * Constructs an OperationResult model.
     *
     * @param resultId The identifier which contains the result of the model/analyze operation.
     */
    public OperationResult(String resultId) {
        this.resultId = Objects.requireNonNull(resultId, "'resultId' cannot be null.");
    }

    /**
     * Get the resultId.
     *
     * @return the resultId.
     */
    public String getResultId() {
        return this.resultId;
    }
}
