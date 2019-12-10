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

    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }

    public TextAnalyticsClientOptions setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        return this;
    }

    public String getDefaultCountryHint() {
        return this.defaultCountryHint;
    }

    public TextAnalyticsClientOptions setDefaultCountryHint(String defaultCountryHint) {
        this.defaultCountryHint = defaultCountryHint;
        return this;
    }
}
