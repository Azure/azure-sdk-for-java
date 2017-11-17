/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition.LangMethodType;
import com.microsoft.azure.management.cdn.implementation.CheckNameAvailabilityOutputInner;

/**
 * Result of the custom domain validation.
 */
@LangDefinition
public class CheckNameAvailabilityResult {
    private CheckNameAvailabilityOutputInner inner;

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
    @LangMethodDefinition(AsType = LangMethodType.Property)
    public boolean nameAvailable() {
        return this.inner.nameAvailable();
    }

    /**
     * Get the reason value.
     *
     * @return the reason value
     */
    @LangMethodDefinition(AsType = LangMethodType.Property)
    public String reason() {
        return this.inner.reason();
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    @LangMethodDefinition(AsType = LangMethodType.Property)
    public String message() {
        return this.inner.message();
    }

}
