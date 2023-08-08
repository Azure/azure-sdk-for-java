// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.Locale;

/**
 * The {@link DetectLanguageInput} model.
 */
@Immutable
public final class DetectLanguageInput {
    /*
     * Unique, non-empty document identifier.
     */
    private final String id;

    /*
     * The text property.
     */
    private final String text;

    /*
     * The countryHint property.
     */
    private final String countryHint;

    /**
     * Creates an input for detect language that will takes {@code id} and {@code document} as required inputs.
     *
     * @param id Unique, non-empty document identifier.
     * @param text The text property.
     */
    public DetectLanguageInput(String id, String text) {
        this(id, text, null);
    }

    /**
     * Creates an input for detect language that will takes {@code id}, {@code document} and {@code countryHint}.
     *
     * @param id Unique, non-empty document identifier.
     * @param text The text property.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified. To remove this behavior you can reset this parameter by setting this value to empty string
     * {@code countryHint} = "" or "none".
     */
    public DetectLanguageInput(String id, String text, String countryHint) {
        this.id = id;
        this.text = text;
        this.countryHint = countryHint;
    }

    /**
     * Gets the id property: Unique, non-empty document identifier.
     *
     * @return The id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the text property: The text property.
     *
     * @return The text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets the countryHint property: The {@code countryHint} property.
     *
     * @return The {@code countryHint} value.
     */
    public String getCountryHint() {
        return this.countryHint;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Text = %s, Id = %s, Country Hint = %s",
            this.getText(), this.getId(), this.getCountryHint());
    }
}
