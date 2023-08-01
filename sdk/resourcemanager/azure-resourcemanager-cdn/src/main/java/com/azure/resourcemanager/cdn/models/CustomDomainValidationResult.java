// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.resourcemanager.cdn.fluent.models.ValidateCustomDomainOutputInner;

/**
 * The {@link CdnProfile#validateEndpointCustomDomain(String, String)} action result.
 */
public class CustomDomainValidationResult {
    private final ValidateCustomDomainOutputInner inner;

    /**
     * Construct CustomDomainValidationResult object from server response object.
     *
     * @param inner server response for CustomDomainValidation request.
     */
    public CustomDomainValidationResult(ValidateCustomDomainOutputInner inner) {
        this.inner = inner;
    }

    /**
     * Get the customDomainValidated value.
     *
     * @return the customDomainValidated value
     */
    public boolean customDomainValidated() {
        return this.inner.customDomainValidated();
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
