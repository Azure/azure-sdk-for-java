// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters used across all Batch methods.
 */
public class BatchBaseOptions {
    private Integer timeOutInSeconds;

    /**
     * Gets the maximum time that the server can spend processing the request, in seconds. The default is 30 seconds.
     *
     * @return The maximum time that the server can spend processing the request, in seconds.
     */
    public Integer getTimeOutInSeconds() {
        return timeOutInSeconds;
    }

    /**
     * Sets the maximum time that the server can spend processing the request, in seconds. The default is 30 seconds.
     *
     * @param timeOutInSeconds The maximum time that the server can spend processing the request, in seconds.
     * @return The {@link BatchBaseOptions} object itself, allowing for method chaining.
     */
    public BatchBaseOptions setTimeOutInSeconds(Integer timeOutInSeconds) {
        this.timeOutInSeconds = timeOutInSeconds;
        return this;
    }

}
