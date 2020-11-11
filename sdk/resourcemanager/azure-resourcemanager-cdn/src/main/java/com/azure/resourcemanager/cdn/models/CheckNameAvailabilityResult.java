// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.resourcemanager.cdn.fluent.models.CheckNameAvailabilityOutputInner;

/**
 * Result of the custom domain validation.
 */
public class CheckNameAvailabilityResult {
    private final CheckNameAvailabilityOutputInner inner;

    /**
     * Construct CheckNameAvailabilityResult object from server response object.
     *
     * @param inner server response for CheckNameAvailability request.
     */
    public CheckNameAvailabilityResult(CheckNameAvailabilityOutputInner inner) {
        this.inner = inner;
    }

    /**
     * Indicates whether the name is available.
     *
     * @return the nameAvailable value
     */
    public boolean nameAvailable() {
        return this.inner.nameAvailable();
    }

    /**
     * Get the reason value.
     *
     * @return the reason value
     */
    public String reason() {
        return this.inner.reason();
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String message() {
        return this.inner.message();
    }

}
