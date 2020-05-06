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
    public CustomFormModelInfo(final String modelId, final CustomFormModelStatus status, final OffsetDateTime createdOn,
        final OffsetDateTime lastUpdatedOn) {
        this.modelId = modelId;
        this.status = status;
        this.createdOn = createdOn;
        this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Get the model identifier.
     *
     * @return the modelId value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Get the Status of the model.
     *
     * @return the status value.
     */
    public CustomFormModelStatus getStatus() {
        return this.status;
    }

    /**
     * Get the date and time (UTC) when the model was created.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Get the date and time (UTC) when the status was last updated.
     *
     * @return the lastUpdatedDateTime value.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return this.lastUpdatedOn;
    }

}
