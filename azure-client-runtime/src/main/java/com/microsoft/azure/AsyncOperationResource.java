/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

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
    public void status(String status) {
        this.status = status;
    }
}
