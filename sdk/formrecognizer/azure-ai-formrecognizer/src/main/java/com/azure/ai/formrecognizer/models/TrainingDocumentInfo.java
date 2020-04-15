// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.Collections;

/**
 * The TrainingDocumentInfo model.
 */
@Immutable
public final class TrainingDocumentInfo {

    /*
     * Training document name.
     */
    private final String name;

    /*
     * Status of the training operation.
     */
    private final TrainingStatus trainingStatus;

    /*
     * Total number of pages trained.
     */
    private final int pageCount;

    /*
     * List of errors.
     */
    private final IterableStream<FormRecognizerError> documentError;

    /**
     * Constructs a TrainingDocumentInfo object.
     *
     * @param name Training document name.
     * @param trainingStatus Status of the training operation.
     * @param pageCount Total number of pages trained.
     * @param documentError List of errors.
     */
    public TrainingDocumentInfo(final String name, final TrainingStatus trainingStatus, final int pageCount,
                                final IterableStream<FormRecognizerError> documentError) {
        this.name = name;
        this.trainingStatus = trainingStatus;
        this.pageCount = pageCount;
        this.documentError = documentError == null
            ? new IterableStream<FormRecognizerError>(Collections.emptyList())
            : documentError;
    }

    /**
     * Get the documentName property: Training document name.
     *
     * @return the documentName value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the status property: Status of the training operation.
     *
     * @return the status value.
     */
    public TrainingStatus getTrainingStatus() {
        return this.trainingStatus;
    }

    /**
     * Get the pageCount property: Total number of pages trained.
     *
     * @return the pages value.
     */
    public int getPageCount() {
        return this.pageCount;
    }

    /**
     * Get the errors property: List of errors.
     *
     * @return the errors value.
     */
    public IterableStream<FormRecognizerError> getDocumentError() {
        return this.documentError;
    }
}
