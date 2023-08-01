// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;


import com.azure.ai.formrecognizer.documentanalysis.implementation.util.OperationDetailsHelper;
import com.azure.core.models.ResponseError;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The OperationDetails model.
 */
public class OperationDetails {
    private String operationId;
    private OperationStatus status;
    private Integer percentCompleted;
    private OffsetDateTime createdOn;
    private ResponseError error;
    private String resourceLocation;
    private OffsetDateTime lastUpdatedOn;
    private OperationKind kind;
    private Map<String, String> tags;

   /**
     * Get the Date and time (UTC) when the analyze operation was submitted.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    private void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }
    private void setError(ResponseError error) {
        this.error = error;
    }

    /**
     * Get the error property: Encountered error.
     *
     * @return the error value.
     */
    public ResponseError getError() {
        return error;
    }

    /**
     * Get the operationId property: Operation ID.
     *
     * @return the operationId value.
     */
    public String getOperationId() {
        return operationId;
    }

    private void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get the status property: Operation status.
     *
     * @return the status value.
     */
    public OperationStatus getStatus() {
        return status;
    }

    private void setStatus(OperationStatus status) {
        this.status = status;
    }

    /**
     * Get the percentCompleted property: Operation progress (0-100).
     *
     * @return the percentCompleted value.
     */
    public Integer getPercentCompleted() {
        return percentCompleted;
    }

    private void setPercentCompleted(Integer percentCompleted) {
        this.percentCompleted = percentCompleted;
    }

    /**
     * Get the lastUpdatedDateTime property: Date and time (UTC) when the status was last updated.
     *
     * @return the lastUpdatedDateTime value.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    private void setLastUpdatedOn(OffsetDateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Get the kind property: Type of operation.
     *
     * @return the kind value.
     */
    public OperationKind getKind() {
        return kind;
    }

    private void setKind(OperationKind kind) {
        this.kind = kind;
    }

    /**
     * Get the resourceLocation property: URL of the resource targeted by this operation.
     *
     * @return the resourceLocation value.
     */
    public String getResourceLocation() {
        return resourceLocation;
    }

    private void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    /**
     * Get the list of key-value tag attributes associated with the document model.
     *
     * @return the tags value.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the list of key-value tag attributes associated with the document model
     *
     * @param tags the tags value to set.
     */
    private void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    static {
        OperationDetailsHelper.setAccessor(new OperationDetailsHelper.OperationDetailsAccessor() {
            public void setCreatedOn(OperationDetails operationDetails, OffsetDateTime createdOn) {
                operationDetails.setCreatedOn(createdOn);
            }

            public void setError(OperationDetails operationDetails, ResponseError error) {
                operationDetails.setError(error);
            }

            public void setOperationId(OperationDetails operationDetails, String operationId) {
                operationDetails.setOperationId(operationId);
            }

            public void setStatus(OperationDetails operationDetails, OperationStatus status) {
                operationDetails.setStatus(status);
            }

            public void setPercentCompleted(OperationDetails operationDetails, Integer percentCompleted) {
                operationDetails.setPercentCompleted(percentCompleted);
            }

            public void setLastUpdatedOn(OperationDetails operationDetails, OffsetDateTime lastUpdatedOn) {
                operationDetails.setLastUpdatedOn(lastUpdatedOn);
            }

            public void setKind(OperationDetails operationDetails, OperationKind kind) {
                operationDetails.setKind(kind);
            }

            public void setResourceLocation(OperationDetails operationDetails, String resourceLocation) {
                operationDetails.setResourceLocation(resourceLocation);
            }

            public void setTags(OperationDetails operationDetails, Map<String, String> tags) {
                operationDetails.setTags(tags);
            }
        });
    }
}
