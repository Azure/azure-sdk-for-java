// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final List<FormRecognizerError> documentErrors;

    /**
     * Constructs a TrainingDocumentInfo object.
     *
     * @param name Training document name.
     * @param trainingStatus Status of the training operation.
     * @param pageCount Total number of pages trained.
     * @param documentErrors List of errors.
     */
    public TrainingDocumentInfo(final String name, final TrainingStatus trainingStatus, final int pageCount,
                                final List<FormRecognizerError> documentErrors) {
        this.name = name;
        this.trainingStatus = trainingStatus;
        this.pageCount = pageCount;
        this.documentErrors = documentErrors == null ? null
            : Collections.unmodifiableList(documentErrors);
    }

    /**
     * Get the training document name.
     *
     * @return the documentName value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the status of the training operation.
     *
     * @return the status value.
     */
    public TrainingStatus getTrainingStatus() {
        return this.trainingStatus;
    }

    /**
     * Get the total number of pages trained.
     *
     * @return the pages value.
     */
    public int getPageCount() {
        return this.pageCount;
    }

    /**
     * Get the list of errors.
     *
     * @return the unmodifiable list of errors.
     */
    public List<FormRecognizerError> getDocumentErrors() {
        return this.documentErrors;
    }
}
