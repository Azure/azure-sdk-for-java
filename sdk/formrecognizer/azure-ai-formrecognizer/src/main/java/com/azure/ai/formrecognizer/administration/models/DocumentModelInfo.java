// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentModelInfoHelper;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Model summary for the built and prebuilt models.
 */
public final class DocumentModelInfo {
    /*
     * Unique model identifier.
     */
    private String modelId;

    /*
     * Model description.
     */
    private String description;

    /*
     * Date and time (UTC) when the model was created.
     */
    private OffsetDateTime createdOn;

    /*
     * List of key-value tag attributes associated with the model.
     */
    private Map<String, String> tags;

    /**
     * Get the unique model identifier.
     *
     * @return the modelId value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Set the modelId property: Unique model identifier.
     *
     * @param modelId the modelId value to set.
     * @return the ModelSummary object itself.
     */
    void setModelId(String modelId) {
        this.modelId = modelId;
    }

    /**
     * Get the description property: Model description.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: Model description.
     *
     * @param description the description value to set.
     * @return the ModelSummary object itself.
     */
    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the createdDateTime property: Date and time (UTC) when the model was created.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Set the createdDateTime property: Date and time (UTC) when the model was created.
     *
     * @param createdOn the createdDateTime value to set.
     * @return the ModelSummary object itself.
     */
    void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Get the user defined attributes associated with the model.
     *
     * @return the tags value.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    static {
        DocumentModelInfoHelper.setAccessor(new DocumentModelInfoHelper.DocumentModelInfoAccessor() {
            @Override
            public void setModelId(DocumentModelInfo documentModelInfo, String modelId) {
                documentModelInfo.setModelId(modelId);
            }

            @Override
            public void setDescription(DocumentModelInfo documentModelInfo, String description) {
                documentModelInfo.setDescription(description);
            }

            @Override
            public void setCreatedOn(DocumentModelInfo documentModelInfo, OffsetDateTime createdDateTime) {
                documentModelInfo.setCreatedOn(createdDateTime);
            }

            @Override
            public void setTags(DocumentModelInfo documentModelInfo, Map<String, String> tags) {
                documentModelInfo.setTags(tags);
            }
        });
    }
}
