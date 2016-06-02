/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Response for a restore site request.
 */
@JsonFlatten
public class RestoreResponseInner extends Resource {
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
    public String operationId() {
        return this.operationId;
    }

    /**
     * Set the operationId value.
     *
     * @param operationId the operationId value to set
     * @return the RestoreResponseInner object itself.
     */
    public RestoreResponseInner withOperationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

}
