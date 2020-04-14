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
    private final ModelTrainingStatus status;

    /*
     * Date and time (UTC) when the model was created.
     */
    private final OffsetDateTime createdOn;

    /*
     * Date and time (UTC) when the status was last updated.
     */
    private final OffsetDateTime lastUpdatedOn;

    /**
     * Constructs a {@link CustomFormModelInfo} box object.
     *
     * @param modelId The model identifier.
     * @param status The status of the model.
     * @param createdOn The date and time (UTC) when the model was created.
     * @param lastUpdatedOn The date and time (UTC) when the status was last updated.
     */
    public CustomFormModelInfo(final String modelId, final ModelTrainingStatus status, final OffsetDateTime createdOn,
        final OffsetDateTime lastUpdatedOn) {
        this.modelId = modelId;
        this.status = status;
        this.createdOn = createdOn;
        this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Get the modelId property: Model identifier.
     *
     * @return the modelId value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Get the status property: Status of the model.
     *
     * @return the status value.
     */
    public ModelTrainingStatus getStatus() {
        return this.status;
    }

    /**
     * Get the createdDateTime property: Date and time (UTC) when the model was
     * created.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Get the lastUpdatedDateTime property: Date and time (UTC) when the
     * status was last updated.
     *
     * @return the lastUpdatedDateTime value.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return this.lastUpdatedOn;
    }

}
