// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;

/**
 * A helper class to represent the HTTP response headers returned by the service.
 */
@Fluent
public final class DigitalTwinsResponseHeaders {
    /*
     * The ETag property.
     */
    private String eTag;

    /**
     * Creates a new instance of {@link DigitalTwinsResponseHeaders}.
     */
    public DigitalTwinsResponseHeaders() {
    }

    /**
     * Get the eTag property: The ETag property.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag property: The ETag property.
     *
     * @param eTag the eTag value to set.
     * @return the DigitalTwinsResponseHeaders object itself.
     */
    public DigitalTwinsResponseHeaders setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
