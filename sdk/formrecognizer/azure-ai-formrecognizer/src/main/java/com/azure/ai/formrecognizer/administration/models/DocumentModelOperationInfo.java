// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;


import com.azure.ai.formrecognizer.implementation.util.ModelOperationHelper;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The DocumentModelOperationInfo model.
 */
public final class DocumentModelOperationInfo extends DocumentModelOperationSummary {

    /*
     * Unique model identifier.
     */
    private String modelId;

    /*
     * Model description.
     */
    private String description;

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
        ModelOperationHelper.setAccessor(new ModelOperationHelper.ModelOperationAccessor() {
            @Override
            public void setModelId(DocumentModelOperationInfo modelOperationInfo, String modelId) {
                modelOperationInfo.setModelId(modelId);
            }

            @Override
            public void setDescription(DocumentModelOperationInfo modelOperationInfo, String description) {
                modelOperationInfo.setDescription(description);
            }

            @Override
            public void setCreatedOn(DocumentModelOperationInfo modelOperationInfo, OffsetDateTime createdOn) {
                modelOperationInfo.setCreatedOn(createdOn);
            }

            @Override
            public void setDocTypes(DocumentModelOperationInfo modelOperationInfo, Map<String, DocTypeInfo> docTypes) {
                modelOperationInfo.setDocTypes(docTypes);
            }

            @Override
            public void setError(DocumentModelOperationInfo modelOperationInfo, DocumentModelOperationError error) {
                modelOperationInfo.setError(error);
            }

            @Override
            public void setOperationId(DocumentModelOperationInfo modelOperationInfo, String operationId) {
                modelOperationInfo.setOperationId(operationId);
            }

            @Override
            public void setStatus(DocumentModelOperationInfo modelOperationInfo, ModelOperationStatus status) {
                modelOperationInfo.setStatus(status);
            }

            @Override
            public void setPercentCompleted(DocumentModelOperationInfo modelOperationInfo, Integer percentCompleted) {
                modelOperationInfo.setPercentCompleted(percentCompleted);
            }

            @Override
            public void setLastUpdatedOn(DocumentModelOperationInfo modelOperationInfo, OffsetDateTime lastUpdatedOn) {
                modelOperationInfo.setLastUpdatedOn(lastUpdatedOn);
            }

            @Override
            public void setKind(DocumentModelOperationInfo modelOperationInfo, ModelOperationKind kind) {
                modelOperationInfo.setKind(kind);
            }

            @Override
            public void setResourceLocation(DocumentModelOperationInfo modelOperationInfo, String resourceLocation) {
                modelOperationInfo.setResourceLocation(resourceLocation);
            }
        });
    }
}
