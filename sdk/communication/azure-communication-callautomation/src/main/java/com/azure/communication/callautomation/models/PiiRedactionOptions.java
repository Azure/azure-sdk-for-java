// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * PII redaction configuration options.
 */
@Fluent
public final class PiiRedactionOptions {
    /*
    * Gets or sets a value indicating whether PII redaction is enabled.
    */
    private Boolean enable;

    /*
     * Gets or sets the type of PII redaction to be used.
     */
    private RedactionType redactionType;

    /**
     * Creates an instance of PiiRedactionOptions class.
     */
    public PiiRedactionOptions() {
    }

    /**
     * Get the enable property: Gets or sets a value indicating whether PII redaction is enabled.
     * 
     * @return the enable value.
     */
    public Boolean isEnabled() {
        return this.enable;
    }

    /**
     * Set the enable property: Gets or sets a value indicating whether PII redaction is enabled.
     * 
     * @param enable the enable value to set.
     * @return the PiiRedactionOptions object itself.
     */
    public PiiRedactionOptions setEnabled(Boolean enable) {
        this.enable = enable;
        return this;
    }

    /**
     * Get the redactionType property: Gets or sets the type of PII redaction to be used.
     * 
     * @return the redactionType value.
     */
    public RedactionType getRedactionType() {
        return this.redactionType;
    }

    /**
     * Set the redactionType property: Gets or sets the type of PII redaction to be used.
     * 
     * @param redactionType the redactionType value to set.
     * @return the PiiRedactionOptions object itself.
     */
    public PiiRedactionOptions setRedactionType(RedactionType redactionType) {
        this.redactionType = redactionType;
        return this;
    }
}
