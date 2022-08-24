// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentModelOperationSummaryHelper;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;
import java.util.Map;

/** DocumentModelOperationSummary. */
@Immutable
public class DocumentModelOperationSummary {
    /*
     * Operation ID
     */
    private String operationId;

    /*
     * Operation status.
     */
    private DocumentOperationStatus status;

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
    private DocumentOperationKind kind;

    /*
     * URL of the resource targeted by this operation.
     */
    private String resourceLocation;
    private Map<String, String> tags;

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
     */
    private void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get the status property: Operation status.
     *
     * @return the status value.
     */
    public DocumentOperationStatus getStatus() {
        return this.status;
    }

    /**
     * Set the status property: Operation status.
     *
     * @param status the status value to set.
     */
    private void setStatus(DocumentOperationStatus status) {
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
     */
    private void setPercentCompleted(Integer percentCompleted) {
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
     */
    private void setCreatedOn(OffsetDateTime createdOn) {
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
     */
    private void setLastUpdatedOn(OffsetDateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Get the kind property: Type of operation.
     *
     * @return the kind value.
     */
    public DocumentOperationKind getKind() {
        return this.kind;
    }

    /**
     * Set the kind property: Type of operation.
     *
     * @param kind the kind value to set.
     */
    private void setKind(DocumentOperationKind kind) {
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
     */
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
        DocumentModelOperationSummaryHelper.setAccessor(new DocumentModelOperationSummaryHelper.DocumentModelOperationSummaryAccessor() {
            @Override
            public void setOperationId(DocumentModelOperationSummary documentModelOperationSummary, String operationId) {
                documentModelOperationSummary.setOperationId(operationId);
            }

            @Override
            public void setStatus(DocumentModelOperationSummary documentModelOperationSummary, DocumentOperationStatus status) {
                documentModelOperationSummary.setStatus(status);
            }

            @Override
            public void setPercentCompleted(DocumentModelOperationSummary documentModelOperationSummary, Integer percentCompleted) {
                documentModelOperationSummary.setPercentCompleted(percentCompleted);
            }

            @Override
            public void setCreatedOn(DocumentModelOperationSummary documentModelOperationSummary, OffsetDateTime createdOn) {
                documentModelOperationSummary.setCreatedOn(createdOn);
            }

            @Override
            public void setLastUpdatedOn(DocumentModelOperationSummary documentModelOperationSummary, OffsetDateTime lastUpdatedOn) {
                documentModelOperationSummary.setLastUpdatedOn(lastUpdatedOn);
            }

            @Override
            public void setKind(DocumentModelOperationSummary documentModelOperationSummary, DocumentOperationKind kind) {
                documentModelOperationSummary.setKind(kind);
            }

            @Override
            public void setResourceLocation(DocumentModelOperationSummary documentModelOperationSummary, String resourceLocation) {
                documentModelOperationSummary.setResourceLocation(resourceLocation);
            }

            @Override
            public void setTags(DocumentModelOperationSummary documentModelOperationSummary, Map<String, String> tags) {
                documentModelOperationSummary.setTags(tags);
            }
        });
    }
}
