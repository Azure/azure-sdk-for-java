// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.search.documents.implementation.converters.LuceneStandardTokenizerHelper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Breaks text following the Unicode Text Segmentation rules. This tokenizer is
 * implemented using Apache Lucene.
 */
@Fluent
public final class LuceneStandardTokenizer extends LexicalTokenizer {
    private String odataType;

    /*
     * The maximum token length. Default is 255. Tokens longer than the maximum
     * length are split.
     */
    @JsonProperty(value = "maxTokenLength")
    private Integer maxTokenLength;

    static {
        LuceneStandardTokenizerHelper.setAccessor(new LuceneStandardTokenizerHelper.LuceneStandardTokenizerAccessor() {
            @Override
            public void setODataType(LuceneStandardTokenizer tokenizer, String odataType) {
                tokenizer.setODataType(odataType);
            }

            @Override
            public String getODataType(LuceneStandardTokenizer standardTokenizer) {
                return standardTokenizer.getODataType();
            }
        });
    }

    /**
     * Constructor of {@link LuceneStandardTokenizer}.
     *
     * @param name The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @JsonCreator
    public LuceneStandardTokenizer(@JsonProperty(value = "name") String name) {
        super(name);
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

    /**
     * The private setter to set the odataType property
     * via {@link LuceneStandardTokenizerHelper.LuceneStandardTokenizerAccessor}.
     *
     * @param odataType The OData type.
     */
    private void setODataType(String odataType) {
        this.odataType = odataType;
    }

    /**
     * The private getter to get the odataType property
     * via {@link LuceneStandardTokenizerHelper.LuceneStandardTokenizerAccessor}.
     *
     * @return The OData type.
     */
    private String getODataType() {
        return this.odataType;
    }
}
