// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentModelHelper;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The DocumentModel model.
 */
public final class DocumentModel {

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

    static {
        DocumentModelHelper.setAccessor(new DocumentModelHelper.DocumentModelAccessor() {
            @Override
            public void setModelId(DocumentModel documentModel, String modelId) {
                documentModel.setModelId(modelId);
            }

            @Override
            public void setDescription(DocumentModel documentModel, String description) {
                documentModel.setDescription(description);
            }

            @Override
            public void setCreatedOn(DocumentModel documentModel, OffsetDateTime createdOn) {
                documentModel.setCreatedOn(createdOn);
            }

            @Override
            public void setDocTypes(DocumentModel documentModel, Map<String, DocTypeInfo> docTypes) {
                documentModel.setDocTypes(docTypes);
            }
        });
    }
}
