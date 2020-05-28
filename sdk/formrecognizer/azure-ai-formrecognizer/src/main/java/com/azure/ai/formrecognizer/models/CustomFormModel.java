// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The CustomFormModel
 */
@Immutable
public final class CustomFormModel {

    /*
     * List of errors returned during the training operation.
     */
    private final List<FormRecognizerError> modelError;

    /*
     * Model identifier.
     */
    private final String modelId;

    /*
     * Status of the model.
     */
    private final CustomFormModelStatus modelStatus;

    /*
     * Date and time (UTC) when the training model was requested.
     */
    private final OffsetDateTime requestedOn;

    /*
     * Date and time (UTC) when the model training was completed.
     */
    private final OffsetDateTime completedOn;

    /*
     * List of sub models.
     */
    private final IterableStream<CustomFormSubmodel> submodels;

    /*
     * List of the documents used to train the model.
     */
    private final List<TrainingDocumentInfo> trainingDocuments;

    /**
     * Constructs a CustomFormModel object.
     *
     * @param modelId Model identifier.
     * @param modelStatus Status of the model.
     * @param requestedOn Date and time (UTC) when the training model was requested.
     * @param completedOn Date and time (UTC) when the model training was completed.
     * @param submodels List of sub models.
     * @param modelError List of errors returned during the training operation.
     * @param trainingDocuments List of the documents used to train the model.
     */
    public CustomFormModel(final String modelId, final CustomFormModelStatus modelStatus,
        final OffsetDateTime requestedOn, final OffsetDateTime completedOn,
        final IterableStream<CustomFormSubmodel> submodels, final List<FormRecognizerError> modelError,
        final List<TrainingDocumentInfo> trainingDocuments) {
        this.modelId = modelId;
        this.modelStatus = modelStatus;
        this.requestedOn = requestedOn;
        this.completedOn = completedOn;
        this.submodels = submodels;
        this.modelError = modelError;
        this.trainingDocuments = trainingDocuments;
    }

    /**
     * Get the Model identifier.
     *
     * @return the modelId value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Get the status of the model.
     *
     * @return the status value.
     */
    public CustomFormModelStatus getModelStatus() {
        return this.modelStatus;
    }

    /**
     * Get the Date and time (UTC) when the training model was requested.
     *
     * @return the requestedOn value.
     */
    public OffsetDateTime getRequestedOn() {
        return this.requestedOn;
    }

    /**
     * Get the Date and time (UTC) when the model training was completed.
     *
     * @return the completedOn value.
     */
    public OffsetDateTime getCompletedOn() {
        return this.completedOn;
    }

    /**
     * Get the errors returned during the training operation.
     *
     * @return the errors value.
     */
    public List<FormRecognizerError> getModelError() {
        return this.modelError;
    }

    /**
     * Get the recognized sub models returned during the training operation.
     *
     * @return the sub models value.
     */
    public IterableStream<CustomFormSubmodel> getSubmodels() {
        return this.submodels;
    }

    /**
     * Get the list of the documents used to train the model and any errors reported in each document.
     *
     * @return the trainingDocuments value.
     */
    public List<TrainingDocumentInfo> getTrainingDocuments() {
        return this.trainingDocuments;
    }
}
