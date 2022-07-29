// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;


import com.azure.ai.formrecognizer.implementation.util.ModelOperationDetailsHelper;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The ModelOperationDetails model.
 */
public final class ModelOperationDetails extends ModelOperationSummary {

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

    private DocumentModelOperationError error;

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
    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
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

    void setError(DocumentModelOperationError error) {
        this.error = error;
    }

    /**
     * Get the error property: Encountered error.
     *
     * @return the error value.
     */
    public DocumentModelOperationError getError() {
        return error;
    }

    @Override
    public String getOperationId() {
        return super.getOperationId();
    }

    @Override
    void setOperationId(String operationId) {
        super.setOperationId(operationId);
    }

    @Override
    public ModelOperationStatus getStatus() {
        return super.getStatus();
    }

    @Override
    void setStatus(ModelOperationStatus status) {
        super.setStatus(status);
    }

    @Override
    public Integer getPercentCompleted() {
        return super.getPercentCompleted();
    }

    @Override
    void setPercentCompleted(Integer percentCompleted) {
        super.setPercentCompleted(percentCompleted);
    }

    @Override
    public OffsetDateTime getLastUpdatedOn() {
        return super.getLastUpdatedOn();
    }

    @Override
    void setLastUpdatedOn(OffsetDateTime lastUpdatedOn) {
        super.setLastUpdatedOn(lastUpdatedOn);
    }

    @Override
    public ModelOperationKind getKind() {
        return super.getKind();
    }

    @Override
    void setKind(ModelOperationKind kind) {
        super.setKind(kind);
    }

    @Override
    public String getResourceLocation() {
        return super.getResourceLocation();
    }

    @Override
    void setResourceLocation(String resourceLocation) {
        super.setResourceLocation(resourceLocation);
    }

    static {
        ModelOperationDetailsHelper.setAccessor(new ModelOperationDetailsHelper.ModelOperationDetailsAccessor() {
            @Override
            public void setModelId(ModelOperationDetails modelOperationDetails, String modelId) {
                modelOperationDetails.setModelId(modelId);
            }

            @Override
            public void setDescription(ModelOperationDetails modelOperationDetails, String description) {
                modelOperationDetails.setDescription(description);
            }

            @Override
            public void setCreatedOn(ModelOperationDetails modelOperationDetails, OffsetDateTime createdOn) {
                modelOperationDetails.setCreatedOn(createdOn);
            }

            @Override
            public void setDocTypes(ModelOperationDetails modelOperationDetails, Map<String, DocTypeInfo> docTypes) {
                modelOperationDetails.setDocTypes(docTypes);
            }

            @Override
            public void setError(ModelOperationDetails modelOperationDetails, DocumentModelOperationError error) {
                modelOperationDetails.setError(error);
            }

            @Override
            public void setOperationId(ModelOperationDetails modelOperationDetails, String operationId) {
                modelOperationDetails.setOperationId(operationId);
            }

            @Override
            public void setStatus(ModelOperationDetails modelOperationDetails, ModelOperationStatus status) {
                modelOperationDetails.setStatus(status);
            }

            @Override
            public void setPercentCompleted(ModelOperationDetails modelOperationDetails, Integer percentCompleted) {
                modelOperationDetails.setPercentCompleted(percentCompleted);
            }

            @Override
            public void setLastUpdatedOn(ModelOperationDetails modelOperationDetails, OffsetDateTime lastUpdatedOn) {
                modelOperationDetails.setLastUpdatedOn(lastUpdatedOn);
            }

            @Override
            public void setKind(ModelOperationDetails modelOperationDetails, ModelOperationKind kind) {
                modelOperationDetails.setKind(kind);
            }

            @Override
            public void setResourceLocation(ModelOperationDetails modelOperationDetails, String resourceLocation) {
                modelOperationDetails.setResourceLocation(resourceLocation);
            }
        });
    }
}
