/**
 * Object]
 */

package com.microsoft.azure.management.website.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Response for a restore site request.
 */
@JsonFlatten
public class RestoreResponse extends Resource {
    /**
     * When server starts the restore process, it will return an OperationId
     * identifying that particular restore operation.
     */
    @JsonProperty(value = "properties.operationId")
    private String operationId;

    /**
     * Get the operationId value.
     *
     * @return the operationId value
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * Set the operationId value.
     *
     * @param operationId the operationId value to set
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

}
