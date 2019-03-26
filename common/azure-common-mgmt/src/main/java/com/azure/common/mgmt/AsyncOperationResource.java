/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A deserialized POJO representation of an asynchronous operation.
 */
public class AsyncOperationResource {
    @JsonProperty(value = "status")
    private String status;

    /**
     * The status of the asynchronous operation.
     * @return The status of the asynchronous operation.
     */
    public String status() {
        return status;
    }

    /**
     * Set the status of the asynchronous operation.
     * @param status The status of the asynchronous operation.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty(value = "id")
    private String id;

    /**
     * @return The resource's id.
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of this resource.
     * @param id The id of this resource.
     */
    public void setId(String id) {
        this.id = id;
    }
}
