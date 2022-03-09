// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Specifies some text and analysis components used to break that text into
 * tokens.
 */
@Fluent
public final class AnalyzeTextOptions {
    /*
     * The text to break into tokens.
     */
    @JsonProperty(value = "text", required = true)
    private final String text;

    /*
     * The name of the analyzer to use to break the given text.
     */
    @JsonProperty(value = "analyzer")
    private LexicalAnalyzerName analyzerName;

    /*
     * The name of the tokenizer to use to break the given text.
     */
    @JsonProperty(value = "tokenizer")
    private LexicalTokenizerName tokenizerName;

    /*
     * The name of the normalizer to use to normalize the given text.
     */
    @JsonProperty(value = "normalizer")
    private LexicalNormalizerName normalizerName;

    /*
     * An optional list of token filters to use when breaking the given text.
     */
    @JsonProperty(value = "tokenFilters")
    private List<TokenFilterName> tokenFilters;

    /*
     * An optional list of character filters to use when breaking the given
     * text.
     */
    @JsonProperty(value = "charFilters")
    private List<CharFilterName> charFilters;

    /**
     * Constructor to {@link AnalyzeTextOptions} which takes analyzerName.
     *
     * @param text The text break into tokens.
     * @param analyzerName The name of the analyzer to use to break the given text.
     */
    public AnalyzeTextOptions(String text, LexicalAnalyzerName analyzerName) {
        this.text = text;
        this.analyzerName = analyzerName;
        this.tokenizerName = null;
        this.normalizerName = null;
    }

    /**
     * Constructor to {@link AnalyzeTextOptions} which takes tokenizerName.
     *
     * @param text The text break into tokens.
     * @param tokenizerName The name of the tokenizer to use to break the given text.
     */
    public AnalyzeTextOptions(String text, LexicalTokenizerName tokenizerName) {
        this.text = text;
        this.tokenizerName = tokenizerName;
        this.analyzerName = null;
        this.normalizerName = null;
    }

    /**
     * Constructor to {@link AnalyzeTextOptions} which takes normalizerName.
     *
     * @param text The text break into tokens.
     * @param normalizerName The name of the normalizer to use to break the given text.
     */
    public AnalyzeTextOptions(String text, LexicalNormalizerName normalizerName) {
        this.text = text;
        this.normalizerName = normalizerName;
        this.analyzerName = null;
        this.tokenizerName = null;
    }

    /**
     * Get the text property: The text to break into tokens.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the analyzer name property: The name of the analyzer to use to break the given text.
     *
     * @return the analyzer value.
     */
    public LexicalAnalyzerName getAnalyzerName() {
        return this.analyzerName;
    }

    /**
     * Get the tokenizer name property: The name of the tokenizer to use to break the given text.
     *
     * @return the tokenizer value.
     */
    public LexicalTokenizerName getTokenizerName() {
        return this.tokenizerName;
    }

    /**
     * Get the normalizer name property: The name of the normalizer to use to normalize the given text.
     *
     * @return the normalizer value.
     */
    public LexicalNormalizerName getNormalizer() {
        return this.normalizerName;
    }

    /**
     * Get the tokenFilters property: An optional list of token filters to use when breaking the given text.
     *
     * @return the tokenFilters value.
     */
    public List<TokenFilterName> getTokenFilters() {
        return this.tokenFilters;
    }

    /**
     * Set the tokenFilters property: An optional list of token filters to use when breaking the given text.
     *
     * @param tokenFilters the tokenFilters value to set.
     * @return the AnalyzeRequest object itself.
     */
    public AnalyzeTextOptions setTokenFilters(TokenFilterName... tokenFilters) {
        this.tokenFilters = (tokenFilters == null) ? null : Arrays.asList(tokenFilters);
        return this;
    }

    /**
     * Get the charFilters property: An optional list of character filters to use when breaking the given text.
     *
     * @return the charFilters value.
     */
    public List<CharFilterName> getCharFilters() {
        return this.charFilters;
    }

    /**
     * Set the charFilters property: An optional list of character filters to use when breaking the given text.
     *
     * @param charFilters the charFilters value to set.
     * @return the AnalyzeRequest object itself.
     */
    public AnalyzeTextOptions setCharFilters(CharFilterName... charFilters) {
        this.charFilters = (charFilters == null) ? null : Arrays.asList(charFilters);
        return this;
    }
}
