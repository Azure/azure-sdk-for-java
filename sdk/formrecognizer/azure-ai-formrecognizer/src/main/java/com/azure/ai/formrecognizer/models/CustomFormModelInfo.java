// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * The CustomFormModelInfo model.
 */
@Fluent
public final class CustomFormModelInfo {

    /*
     * Model identifier.
     */
    private UUID modelId;

    /*
     * Status of the model.
     */
    private ModelTrainingStatus status;

    /*
     * Date and time (UTC) when the model was created.
     */
    private OffsetDateTime createdOn;

    /*
     * Date and time (UTC) when the status was last updated.
     */
    private OffsetDateTime lastUpdatedOn;

    public CustomFormModelInfo(final UUID modelId, final ModelTrainingStatus status, final OffsetDateTime createdOn, final OffsetDateTime lastUpdatedOn) {
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
    public UUID getModelId() {
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
