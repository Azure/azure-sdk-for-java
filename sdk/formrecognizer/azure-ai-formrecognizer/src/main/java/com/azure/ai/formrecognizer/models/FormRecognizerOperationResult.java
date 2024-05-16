// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Objects;

/**
 * The FormRecognizerOperationResult model.
 */
@Immutable
public final class FormRecognizerOperationResult {
    /**
     * Identifier which contains the result of the model/analyze operation.
     */
    private final String resultId;

    /**
     * Constructs an OperationResult model.
     *
     * @param resultId The identifier which contains the result of the model/analyze operation.
     */
    public FormRecognizerOperationResult(String resultId) {
        this.resultId = Objects.requireNonNull(resultId, "'resultId' cannot be null.");
    }

    /**
     * Gets an ID representing the operation that can be used to poll for the status
     * of the long-running operation.
     *
     * @return the resultId.
     */
    public String getResultId() {
        return this.resultId;
    }
}
