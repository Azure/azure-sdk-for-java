// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The TextAnalyticsClientOptions model.
 */
@Fluent
public final class TextAnalyticsClientOptions {
    private String defaultLanguage;
    private String defaultCountryHint;

    /**
     * Get the default language.
     *
     * @return the default language
     */
    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }

    /**
     * Set the default language.
     *
     * @param defaultLanguage the default language
     * @return the TextAnalyticsClientOptions object itself
     */
    public TextAnalyticsClientOptions setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        return this;
    }

    /**
     * Get the default country hint.
     *
     * @return the default country hint
     */
    public String getDefaultCountryHint() {
        return this.defaultCountryHint;
    }

    /**
     * Set the default country hint.
     *
     * @param defaultCountryHint the default country hint
     * @return the TextAnalyticsClientOptions object itself
     */
    public TextAnalyticsClientOptions setDefaultCountryHint(String defaultCountryHint) {
        this.defaultCountryHint = defaultCountryHint;
        return this;
    }
}
