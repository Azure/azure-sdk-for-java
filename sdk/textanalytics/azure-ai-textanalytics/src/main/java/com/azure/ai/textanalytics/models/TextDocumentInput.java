// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import java.util.Locale;

/**
 * Contains an input document to be analyzed by the service.
 */
public final class TextDocumentInput {

    /*
     * A unique, non-empty document identifier.
     */
    private final String id;

    /*
     * The document to process.
     */
    private final String text;

    /*
     * (Optional) This is the 2 letter ISO 639-1 representation of a language.
     * For example, use "en" for English; "es" for Spanish etc. If not set, use
     * "en" for English as default.
     */
    private String language;

    /**
     * Creates a {@code TextDocumentInput} model that describes the documents.
     *
     * @param id A unique, non-empty document identifier.
     * @param text The document to process.
     */
    public TextDocumentInput(String id, String text) {
        this.id = id;
        this.text = text;
    }

    /**
     * Get the id property: A unique, non-empty document identifier.
     *
     * @return The id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the text property: The document to process.
     *
     * @return The text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the language property: (Optional) This is the 2 letter ISO 639-1
     * representation of a language. For example, use "en" for English; "es"
     * for Spanish etc. If not set, use "en" for English as default.
     *
     * @return The language value.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Set the language property: (Optional) This is the 2 letter ISO 639-1
     * representation of a language. For example, use "en" for English; "es"
     * for Spanish etc. If not set, use "en" for English as default.
     *
     * @param language Optional. This is the 2 letter ISO 639-1 representation of a language.
     *
     * @return The object {@link TextDocumentInput} itself.
     */
    public TextDocumentInput setLanguage(String language) {
        this.language = language;
        return this;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Text = %s, Id = %s, Language = %s",
            this.getText(), this.getId(), this.getLanguage());
    }
}
