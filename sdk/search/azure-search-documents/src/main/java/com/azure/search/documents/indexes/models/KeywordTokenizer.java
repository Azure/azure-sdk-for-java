// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Emits the entire input as a single token. This tokenizer is implemented
 * using Apache Lucene.
 */
@Fluent
public final class KeywordTokenizer extends LexicalTokenizer {
    private final String odataType;

    /*
     * The maximum token length. Default is 256. Tokens longer than the maximum
     * length are split. The maximum token length that can be used is 300
     * characters.
     */
    @JsonProperty(value = "maxTokenLength")
    private Integer maxTokenLength;

    /**
     * Constructor for {@link KeywordTokenizer}.
     */
    public KeywordTokenizer() {
        odataType = "#Microsoft.Azure.Search.KeywordTokenizerV2";
    }

    /**
     * Get the maxTokenLength property: The maximum token length. Default is
     * 256. Tokens longer than the maximum length are split. The maximum token
     * length that can be used is 300 characters.
     *
     * @return the maxTokenLength value.
     */
    public Integer getMaxTokenLength() {
        return this.maxTokenLength;
    }

    /**
     * Set the maxTokenLength property: The maximum token length. Default is
     * 256. Tokens longer than the maximum length are split. The maximum token
     * length that can be used is 300 characters.
     *
     * @param maxTokenLength the maxTokenLength value to set.
     * @return the KeywordTokenizerV2 object itself.
     */
    public KeywordTokenizer setMaxTokenLength(Integer maxTokenLength) {
        this.maxTokenLength = maxTokenLength;
        return this;
    }
}
