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
     * Date and time (UTC) when the training model was requested.
     */
    private final OffsetDateTime requestedOn;

    /*
     * Date and time (UTC) when the model training was completed.
     */
    private final OffsetDateTime completedOn;

    /**
     * Constructs a {@link CustomFormModelInfo} object.
     *
     * @param modelId The model identifier.
     * @param status The status of the model.
     * @param requestedOn Date and time (UTC) when the training model was requested.
     * @param completedOn Date and time (UTC) when the model training was completed.
     */
    public CustomFormModelInfo(final String modelId, final CustomFormModelStatus status,
        final OffsetDateTime requestedOn, final OffsetDateTime completedOn) {
        this.modelId = modelId;
        this.status = status;
        this.requestedOn = requestedOn;
        this.completedOn = completedOn;
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
     * Get the date and time (UTC) when the training was requested.
     *
     * @return the {@code requestedOn} value.
     */
    public OffsetDateTime getRequestedOn() {
        return this.requestedOn;
    }

    /**
     * Get the date and time (UTC) when the model training was completed.
     *
     * @return the {@code completedOn} value.
     */
    public OffsetDateTime getCompletedOn() {
        return this.completedOn;
    }

}
