/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.cdn.implementation.ValidateCustomDomainOutputInner;

/**
 * The {@link com.microsoft.azure.management.cdn.CdnProfile#validateEndpointCustomDomain(String, String)} action result.
 */
@LangDefinition
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
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public boolean customDomainValidated() {
        return this.inner.customDomainValidated();
    }

    /**
     * Get the reason value.
     *
     * @return the reason value
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String reason() {
        return this.inner.reason();
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String message() {
        return this.inner.message();
    }
}
