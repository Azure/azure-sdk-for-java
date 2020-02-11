// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.Locale;

/**
 * The DetectLanguageInput model.
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
     * Creates an input for detect language that will takes {@code id} and {@code text} as required inputs.
     *
     * @param id unique, non-empty document identifier
     * @param text the text property
     */
    public DetectLanguageInput(String id, String text) {
        this(id, text, null);
    }

    /**
     * Creates an input for detect language that will takes {@code id}, {@code text} and {@code countryHint}.
     *
     * @param id unique, non-empty document identifier
     * @param text the text property
     * @param countryHint the country hint
     */
    public DetectLanguageInput(String id, String text, String countryHint) {
        this.id = id;
        this.text = text;
        this.countryHint = countryHint;
    }

    /**
     * Get the id property: Unique, non-empty document identifier.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the text property: The text property.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the countryHint property: The countryHint property.
     *
     * @return the countryHint value.
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
