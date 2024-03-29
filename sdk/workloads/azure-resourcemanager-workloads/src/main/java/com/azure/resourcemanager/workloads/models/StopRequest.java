// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.workloads.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Stop SAP instance(s) request body. */
@Fluent
public final class StopRequest {
    /*
     * This parameter defines how long (in seconds) the soft shutdown waits until the RFC/HTTP clients no longer
     * consider the server for calls with load balancing. Value 0 means that the kernel does not wait, but goes
     * directly into the next shutdown state, i.e. hard stop.
     */
    @JsonProperty(value = "softStopTimeoutSeconds")
    private Long softStopTimeoutSeconds;

    /** Creates an instance of StopRequest class. */
    public StopRequest() {
    }

    /**
     * Get the softStopTimeoutSeconds property: This parameter defines how long (in seconds) the soft shutdown waits
     * until the RFC/HTTP clients no longer consider the server for calls with load balancing. Value 0 means that the
     * kernel does not wait, but goes directly into the next shutdown state, i.e. hard stop.
     *
     * @return the softStopTimeoutSeconds value.
     */
    public Long softStopTimeoutSeconds() {
        return this.softStopTimeoutSeconds;
    }

    /**
     * Set the softStopTimeoutSeconds property: This parameter defines how long (in seconds) the soft shutdown waits
     * until the RFC/HTTP clients no longer consider the server for calls with load balancing. Value 0 means that the
     * kernel does not wait, but goes directly into the next shutdown state, i.e. hard stop.
     *
     * @param softStopTimeoutSeconds the softStopTimeoutSeconds value to set.
     * @return the StopRequest object itself.
     */
    public StopRequest withSoftStopTimeoutSeconds(Long softStopTimeoutSeconds) {
        this.softStopTimeoutSeconds = softStopTimeoutSeconds;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}
