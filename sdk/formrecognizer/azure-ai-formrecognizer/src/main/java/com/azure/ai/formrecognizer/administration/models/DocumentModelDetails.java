// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentModelDetailsHelper;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Information about the document model.
 */
public final class DocumentModelDetails {

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

    private Map<String, DocTypeInfo> docTypes;

    /*
     * List of key-value tag attributes associated with the model.
     */
    private Map<String, String> tags;

    /**
     * Get the Unique model identifier.
     *
     * @return the modelId value.
     */
    public String getModelId() {
        return modelId;
    }

    void setModelId(String modelId) {
        this.modelId = modelId;
    }

    /**
     * Get the model description.
     *
     * @return the description value.
     */
    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the Date and time (UTC) when the analyze operation was submitted.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Get the supported document types.
     *
     * @return the docTypes value.
     */
    public Map<String, DocTypeInfo> getDocTypes() {
        return docTypes;
    }

    void setDocTypes(
        Map<String, DocTypeInfo> docTypes) {
        this.docTypes = docTypes;
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
        DocumentModelDetailsHelper.setAccessor(new DocumentModelDetailsHelper.DocumentModelDetailsAccessor() {
            @Override
            public void setModelId(DocumentModelDetails documentModelDetails, String modelId) {
                documentModelDetails.setModelId(modelId);
            }

            @Override
            public void setDescription(DocumentModelDetails documentModelDetails, String description) {
                documentModelDetails.setDescription(description);
            }

            @Override
            public void setCreatedOn(DocumentModelDetails documentModelDetails, OffsetDateTime createdOn) {
                documentModelDetails.setCreatedOn(createdOn);
            }

            @Override
            public void setDocTypes(DocumentModelDetails documentModelDetails, Map<String, DocTypeInfo> docTypes) {
                documentModelDetails.setDocTypes(docTypes);
            }

            @Override
            public void setTags(DocumentModelDetails documentModelDetails, Map<String, String> tags) {
                documentModelDetails.setTags(tags);
            }
        });
    }
}
