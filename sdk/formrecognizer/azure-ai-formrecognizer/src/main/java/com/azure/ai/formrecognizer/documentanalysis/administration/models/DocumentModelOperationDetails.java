// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;


import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentModelOperationDetailsHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.models.ResponseError;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The DocumentModelOperationDetails model.
 */
@Immutable
public final class DocumentModelOperationDetails {
    private String operationId;
    private DocumentOperationStatus status;
    private Integer percentCompleted;
    private OffsetDateTime createdOn;
    private ResponseError error;
    private String resourceLocation;
    private OffsetDateTime lastUpdatedOn;
    private DocumentOperationKind kind;
    private Map<String, String> tags;
    private DocumentModelDetails result;

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
    public DocumentOperationStatus getStatus() {
        return status;
    }

    private void setStatus(DocumentOperationStatus status) {
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
    public DocumentOperationKind getKind() {
        return kind;
    }

    private void setKind(DocumentOperationKind kind) {
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

    /**
     * Get the operation result upon success.
     *
     * @return the result value.
     */
    public DocumentModelDetails getResult() {
        return this.result;
    }

    /**
     * Set the operation result upon success.
     *
     * @param documentModelDetails the result value to set.
     */
    private void setResult(DocumentModelDetails documentModelDetails) {
        this.result = documentModelDetails;
    }

    static {
        DocumentModelOperationDetailsHelper.setAccessor(new DocumentModelOperationDetailsHelper.DocumentModelOperationDetailsAccessor() {
            public void setCreatedOn(DocumentModelOperationDetails documentModelOperationDetails, OffsetDateTime createdOn) {
                documentModelOperationDetails.setCreatedOn(createdOn);
            }

            public void setError(DocumentModelOperationDetails documentModelOperationDetails, ResponseError error) {
                documentModelOperationDetails.setError(error);
            }

            public void setOperationId(DocumentModelOperationDetails documentModelOperationDetails, String operationId) {
                documentModelOperationDetails.setOperationId(operationId);
            }

            public void setStatus(DocumentModelOperationDetails documentModelOperationDetails, DocumentOperationStatus status) {
                documentModelOperationDetails.setStatus(status);
            }

            public void setPercentCompleted(DocumentModelOperationDetails documentModelOperationDetails, Integer percentCompleted) {
                documentModelOperationDetails.setPercentCompleted(percentCompleted);
            }

            public void setLastUpdatedOn(DocumentModelOperationDetails documentModelOperationDetails, OffsetDateTime lastUpdatedOn) {
                documentModelOperationDetails.setLastUpdatedOn(lastUpdatedOn);
            }

            public void setKind(DocumentModelOperationDetails documentModelOperationDetails, DocumentOperationKind kind) {
                documentModelOperationDetails.setKind(kind);
            }

            public void setResourceLocation(DocumentModelOperationDetails documentModelOperationDetails, String resourceLocation) {
                documentModelOperationDetails.setResourceLocation(resourceLocation);
            }

            public void setTags(DocumentModelOperationDetails documentModelOperationDetails, Map<String, String> tags) {
                documentModelOperationDetails.setTags(tags);
            }

            public void setResult(DocumentModelOperationDetails documentModelOperationDetails, DocumentModelDetails documentModelDetails) {
                documentModelOperationDetails.setResult(documentModelDetails);
            }
        });
    }
}
