// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.ai.formrecognizer.implementation.TrainingDocumentInfoHelper;
import com.azure.ai.formrecognizer.models.FormRecognizerError;
import com.azure.core.annotation.Immutable;

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
    private final TrainingStatus status;

    /*
     * Total number of pages trained.
     */
    private final int pageCount;

    /*
     * List of errors.
     */
    private final List<FormRecognizerError> errors;

    private String modelId;

    static {
        TrainingDocumentInfoHelper.setAccessor(new TrainingDocumentInfoHelper.TrainingDocumentInfoAccessor() {
            @Override
            public void setModelId(TrainingDocumentInfo documentInfo, String modelId) {
                documentInfo.setModelId(modelId);
            }
        });
    }

    /**
     * Constructs a TrainingDocumentInfo object.
     *
     * @param name the training document name.
     * @param status the status of the training operation for that document.
     * @param pageCount the total number of pages trained.
     * @param errors the list of errors.
     */
    public TrainingDocumentInfo(final String name, final TrainingStatus status, final int pageCount,
                                final List<FormRecognizerError> errors) {
        this.name = name;
        this.status = status;
        this.pageCount = pageCount;
        this.errors = errors == null ? null : Collections.unmodifiableList(errors);
    }

    /**
     * Get the training document name.
     *
     * @return the training document name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the status of the training operation.
     *
     * @return the status of the training operation.
     */
    public TrainingStatus getStatus() {
        return this.status;
    }

    /**
     * Get the total number of pages trained.
     *
     * @return the total number of pages trained.
     */
    public int getPageCount() {
        return this.pageCount;
    }

    /**
     * Get the list of errors.
     *
     * @return the unmodifiable list of errors.
     */
    public List<FormRecognizerError> getErrors() {
        return this.errors;
    }

    /**
     * Get the Model identifier.
     *
     * @return the {@code modelId} value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * The private setter to set the modelId property
     * via {@link TrainingDocumentInfoHelper.TrainingDocumentInfoAccessor}.
     *
     * @param modelId The the unique identifier of the model.
     */
    private void setModelId(String modelId) {
        this.modelId = modelId;
    }
}
