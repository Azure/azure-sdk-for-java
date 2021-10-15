// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;


import com.azure.ai.formrecognizer.implementation.util.ModelOperationHelper;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The ModelOperation model.
 */
public final class ModelOperation extends ModelOperationInfo {

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

    private FormRecognizerError error;

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

    void setError(FormRecognizerError error) {
        this.error = error;
    }

    /**
     * Get the error property: Encountered error.
     *
     * @return the error value.
     */
    public FormRecognizerError getError() {
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
        ModelOperationHelper.setAccessor(new ModelOperationHelper.ModelOperationAccessor() {
            @Override
            public void setModelId(ModelOperation modelOperation, String modelId) {
                modelOperation.setModelId(modelId);
            }

            @Override
            public void setDescription(ModelOperation modelOperation, String description) {
                modelOperation.setDescription(description);
            }

            @Override
            public void setCreatedOn(ModelOperation modelOperation, OffsetDateTime createdOn) {
                modelOperation.setCreatedOn(createdOn);
            }

            @Override
            public void setDocTypes(ModelOperation modelOperation, Map<String, DocTypeInfo> docTypes) {
                modelOperation.setDocTypes(docTypes);
            }

            @Override
            public void setError(ModelOperation modelOperation, FormRecognizerError error) {
                modelOperation.setError(error);
            }

            @Override
            public void setOperationId(ModelOperation modelOperation, String operationId) {
                modelOperation.setOperationId(operationId);
            }

            @Override
            public void setStatus(ModelOperation modelOperation, ModelOperationStatus status) {
                modelOperation.setStatus(status);
            }

            @Override
            public void setPercentCompleted(ModelOperation modelOperation, Integer percentCompleted) {
                modelOperation.setPercentCompleted(percentCompleted);
            }

            @Override
            public void setLastUpdatedOn(ModelOperation modelOperation, OffsetDateTime lastUpdatedOn) {
                modelOperation.setLastUpdatedOn(lastUpdatedOn);
            }

            @Override
            public void setKind(ModelOperation modelOperation, ModelOperationKind kind) {
                modelOperation.setKind(kind);
            }

            @Override
            public void setResourceLocation(ModelOperation modelOperation, String resourceLocation) {
                modelOperation.setResourceLocation(resourceLocation);
            }
        });
    }
}
