// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentModelSummaryHelper;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Summary information for the built and prebuilt models.
 */
@Immutable
public final class DocumentModelSummary {
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
    private OffsetDateTime expiresOn;


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

    /**
     * Get the Date and time (UTC) when the document model will expire.
     *
     * @return the expiresOn value.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    private void setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
    }

    static {
        DocumentModelSummaryHelper.setAccessor(new DocumentModelSummaryHelper.DocumentModelSummaryAccessor() {
            @Override
            public void setModelId(DocumentModelSummary documentModelSummary, String modelId) {
                documentModelSummary.setModelId(modelId);
            }

            @Override
            public void setDescription(DocumentModelSummary documentModelSummary, String description) {
                documentModelSummary.setDescription(description);
            }

            @Override
            public void setCreatedOn(DocumentModelSummary documentModelSummary, OffsetDateTime createdDateTime) {
                documentModelSummary.setCreatedOn(createdDateTime);
            }

            @Override
            public void setTags(DocumentModelSummary documentModelSummary, Map<String, String> tags) {
                documentModelSummary.setTags(tags);
            }

            @Override
            public void setExpiresOn(DocumentModelSummary documentModelSummary, OffsetDateTime expiresOn) {
                documentModelSummary.setExpiresOn(expiresOn);
            }
        });
    }
}
