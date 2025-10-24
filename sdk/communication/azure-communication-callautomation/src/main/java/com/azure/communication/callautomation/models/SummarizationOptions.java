// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * Configuration options for call summarization.
 */
@Fluent
public final class SummarizationOptions {
    /*
     * Indicating whether end call summary should be enabled.
     */
    private Boolean enableEndCallSummary;

    /*
     * Locale for summarization (e.g., en-US).
     */
    private String locale;

    /**
     * Creates an instance of SummarizationOptions class.
     */

    public SummarizationOptions() {
    }

    /**
     * Get the enableEndCallSummary property: Indicating whether end call summary
     * should be enabled.
     * 
     * @return the enableEndCallSummary value.
     */
    public Boolean isEnableEndCallSummary() {
        return this.enableEndCallSummary;
    }

    /**
     * Set the enableEndCallSummary property: Indicating whether end call summary
     * should be enabled.
     * 
     * @param enableEndCallSummary the enableEndCallSummary value to set.
     * @return the SummarizationOptions object itself.
     */

    public SummarizationOptions setEnableEndCallSummary(Boolean enableEndCallSummary) {
        this.enableEndCallSummary = enableEndCallSummary;
        return this;
    }

    /**
     * Get the locale property: Locale for summarization (e.g., en-US).
     * 
     * @return the locale value.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Set the locale property: Locale for summarization (e.g., en-US).
     * 
     * @param locale the locale value to set.
     * @return the SummarizationOptions object itself.
     */
    public SummarizationOptions setLocale(String locale) {
        this.locale = locale;
        return this;
    }
}
