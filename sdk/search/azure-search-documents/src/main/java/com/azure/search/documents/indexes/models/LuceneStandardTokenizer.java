// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Breaks text following the Unicode Text Segmentation rules. This tokenizer is
 * implemented using Apache Lucene.
 */
@Fluent
public final class LuceneStandardTokenizer extends LexicalTokenizer {
    private final String odataType;

    /*
     * The maximum token length. Default is 255. Tokens longer than the maximum
     * length are split.
     */
    @JsonProperty(value = "maxTokenLength")
    private Integer maxTokenLength;

    /**
     * Constructor for {@link LuceneStandardTokenizer}.
     */
    public LuceneStandardTokenizer() {
        odataType = "#Microsoft.Azure.Search.LuceneStandardTokenizerV2";
    }

    /**
     * Get the maxTokenLength property: The maximum token length. Default is
     * 255. Tokens longer than the maximum length are split.
     *
     * @return the maxTokenLength value.
     */
    public Integer getMaxTokenLength() {
        return this.maxTokenLength;
    }

    /**
     * Set the maxTokenLength property: The maximum token length. Default is
     * 255. Tokens longer than the maximum length are split.
     *
     * @param maxTokenLength the maxTokenLength value to set.
     * @return the LuceneStandardTokenizer object itself.
     */
    public LuceneStandardTokenizer setMaxTokenLength(Integer maxTokenLength) {
        this.maxTokenLength = maxTokenLength;
        return this;
    }
}
