// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * The CustomFormModelInfo model.
 */
@Immutable
public final class CustomFormModelInfo {

    /*
     * Model identifier.
     */
    private final String modelId;

    /*
     * Status of the model.
     */
    private final CustomFormModelStatus status;

    /*
     * Date and time (UTC) when the training of the model was started.
     */
    private final OffsetDateTime trainingStartedOn;

    /*
     * Date and time (UTC) when the model training was completed.
     */
    private final OffsetDateTime trainingCompletedOn;

    /**
     * Constructs a {@link CustomFormModelInfo} object.
     *
     * @param modelId The model identifier.
     * @param status The status of the model.
     * @param trainingStartedOn Date and time (UTC) when the training of the model was started.
     * @param trainingCompletedOn Date and time (UTC) when the model training was completed.
     */
    public CustomFormModelInfo(final String modelId, final CustomFormModelStatus status,
        final OffsetDateTime trainingStartedOn, final OffsetDateTime trainingCompletedOn) {
        this.modelId = modelId;
        this.status = status;
        this.trainingStartedOn = trainingStartedOn;
        this.trainingCompletedOn = trainingCompletedOn;
    }

    /**
     * Get the model identifier.
     *
     * @return the {@code modelId} value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Get the Status of the model.
     *
     * @return the {@code status} value.
     */
    public CustomFormModelStatus getStatus() {
        return this.status;
    }

    /**
     * Get the date and time (UTC) when the training of the model was started.
     *
     * @return the {@code trainingStartedOn} value.
     */
    public OffsetDateTime getTrainingStartedOn() {
        return this.trainingStartedOn;
    }

    /**
     * Get the date and time (UTC) when the model training was completed.
     *
     * @return the {@code trainingCompletedOn} value.
     */
    public OffsetDateTime getTrainingCompletedOn() {
        return this.trainingCompletedOn;
    }

}
