// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.Locale;

/**
 * Contains an input document to be analyzed by the service.
 */
@Immutable
public final class TextDocumentInput {

    /*
     * A unique, non-empty document identifier.
     */
    private final String id;

    /*
     * The input text to process.
     */
    private final String text;

    /*
     * (Optional) This is the 2 letter ISO 639-1 representation of a language.
     * For example, use "en" for English; "es" for Spanish etc. If not set, use
     * "en" for English as default.
     */
    private final String language;

    /**
     * Creates a {@code TextDocumentInput} model that describes the text inputs.
     *
     * @param id a unique, non-empty document identifier
     * @param text the input text to process
     */
    public TextDocumentInput(String id, String text) {
        this(id, text, null);
    }

    /**
     * Creates a {@code TextDocumentInput} model that describes the text inputs.
     *
     * @param id a unique, non-empty document identifier
     * @param text the input text to process
     * @param language Optional. This is the 2 letter ISO 639-1 representation of a language
     */
    public TextDocumentInput(String id, String text, String language) {
        this.id = id;
        this.text = text;
        this.language = language;
    }

    /**
     * Get the id property: A unique, non-empty document identifier.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the text property: The input text to process.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the language property: (Optional) This is the 2 letter ISO 639-1
     * representation of a language. For example, use "en" for English; "es"
     * for Spanish etc. If not set, use "en" for English as default.
     *
     * @return the language value.
     */
    public String getLanguage() {
        return this.language;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Text = %s, Id = %s, Language = %s",
            this.getText(), this.getId(), this.getLanguage());
    }
}
