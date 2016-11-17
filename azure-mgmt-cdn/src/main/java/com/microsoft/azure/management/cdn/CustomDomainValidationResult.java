/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;


import com.microsoft.azure.management.cdn.implementation.ValidateCustomDomainOutputInner;

/**
 * The {@link com.microsoft.azure.management.cdn.CdnProfile#endpointValidateCustomDomain(String, String)} action result.
 */
public class CustomDomainValidationResult {
    private ValidateCustomDomainOutputInner inner;

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
