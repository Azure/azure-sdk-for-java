// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.ModelOperationInfoHelper;

import java.time.OffsetDateTime;

/** ModelOperationInfo. */
public class ModelOperationInfo {
    /*
     * Operation ID
     */
    private String operationId;

    /*
     * Operation status.
     */
    private ModelOperationStatus status;

    /*
     * Operation progress (0-100).
     */
    private Integer percentCompleted;

    /*
     * Date and time (UTC) when the operation was created.
     */
    private OffsetDateTime createdOn;

    /*
     * Date and time (UTC) when the status was last updated.
     */
    private OffsetDateTime lastUpdatedOn;

    /*
     * Type of operation.
     */
    private ModelOperationKind kind;

    /*
     * URL of the resource targeted by this operation.
     */
    private String resourceLocation;

    /**
     * Get the operationId property: Operation ID.
     *
     * @return the operationId value.
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * Set the operationId property: Operation ID.
     *
     * @param operationId the operationId value to set.
     * @return the OperationInfo object itself.
     */
    void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get the status property: Operation status.
     *
     * @return the status value.
     */
    public ModelOperationStatus getStatus() {
        return this.status;
    }

    /**
     * Set the status property: Operation status.
     *
     * @param status the status value to set.
     * @return the OperationInfo object itself.
     */
    void setStatus(ModelOperationStatus status) {
        this.status = status;
    }

    /**
     * Get the percentCompleted property: Operation progress (0-100).
     *
     * @return the percentCompleted value.
     */
    public Integer getPercentCompleted() {
        return this.percentCompleted;
    }

    /**
     * Set the percentCompleted property: Operation progress (0-100).
     *
     * @param percentCompleted the percentCompleted value to set.
     * @return the OperationInfo object itself.
     */
    void setPercentCompleted(Integer percentCompleted) {
        this.percentCompleted = percentCompleted;
    }

    /**
     * Get the createdDateTime property: Date and time (UTC) when the operation was created.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Set the createdDateTime property: Date and time (UTC) when the operation was created.
     *
     * @param createdOn the createdDateTime value to set.
     * @return the OperationInfo object itself.
     */
    void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Get the lastUpdatedDateTime property: Date and time (UTC) when the status was last updated.
     *
     * @return the lastUpdatedDateTime value.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return this.lastUpdatedOn;
    }

    /**
     * Set the lastUpdatedDateTime property: Date and time (UTC) when the status was last updated.
     *
     * @param lastUpdatedOn the lastUpdatedDateTime value to set.
     * @return the OperationInfo object itself.
     */
    void setLastUpdatedOn(OffsetDateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Get the kind property: Type of operation.
     *
     * @return the kind value.
     */
    public ModelOperationKind getKind() {
        return this.kind;
    }

    /**
     * Set the kind property: Type of operation.
     *
     * @param kind the kind value to set.
     * @return the OperationInfo object itself.
     */
    void setKind(ModelOperationKind kind) {
        this.kind = kind;
    }

    /**
     * Get the resourceLocation property: URL of the resource targeted by this operation.
     *
     * @return the resourceLocation value.
     */
    public String getResourceLocation() {
        return this.resourceLocation;
    }

    /**
     * Set the resourceLocation property: URL of the resource targeted by this operation.
     *
     * @param resourceLocation the resourceLocation value to set.
     * @return the OperationInfo object itself.
     */
    void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    static {
        ModelOperationInfoHelper.setAccessor(new ModelOperationInfoHelper.ModelOperationInfoAccessor() {
            @Override
            public void setOperationId(ModelOperationInfo modelOperationInfo, String operationId) {
                modelOperationInfo.setOperationId(operationId);
            }

            @Override
            public void setStatus(ModelOperationInfo modelOperationInfo, ModelOperationStatus status) {
                modelOperationInfo.setStatus(status);
            }

            @Override
            public void setPercentCompleted(ModelOperationInfo modelOperationInfo, Integer percentCompleted) {
                modelOperationInfo.setPercentCompleted(percentCompleted);
            }

            @Override
            public void setCreatedOn(ModelOperationInfo modelOperationInfo, OffsetDateTime createdOn) {
                modelOperationInfo.setCreatedOn(createdOn);
            }

            @Override
            public void setLastUpdatedOn(ModelOperationInfo modelOperationInfo, OffsetDateTime lastUpdatedOn) {
                modelOperationInfo.setLastUpdatedOn(lastUpdatedOn);
            }

            @Override
            public void setKind(ModelOperationInfo modelOperationInfo, ModelOperationKind kind) {
                modelOperationInfo.setKind(kind);
            }

            @Override
            public void setResourceLocation(ModelOperationInfo modelOperationInfo, String resourceLocation) {
                modelOperationInfo.setResourceLocation(resourceLocation);
            }
        });
    }
}
