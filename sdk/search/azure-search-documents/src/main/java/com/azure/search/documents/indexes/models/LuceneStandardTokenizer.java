// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV1;
import com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2;

/**
 * Breaks text following the Unicode Text Segmentation rules. This tokenizer is
 * implemented using Apache Lucene.
 */
@Fluent
public final class LuceneStandardTokenizer extends LexicalTokenizer {
    private final LuceneStandardTokenizerV1 v1Tokenizer;
    private final LuceneStandardTokenizerV2 v2tokenizer;

    LuceneStandardTokenizer(LuceneStandardTokenizerV1 v1Tokenizer) {
        super(v1Tokenizer.getName());

        this.v1Tokenizer = v1Tokenizer;
        this.v2tokenizer = null;
    }

    LuceneStandardTokenizer(LuceneStandardTokenizerV2 v2tokenizer) {
        super(v2tokenizer.getName());

        this.v1Tokenizer = null;
        this.v2tokenizer = v2tokenizer;
    }

    /**
     * Constructor of {@link LuceneStandardTokenizer}.
     *
     * @param name The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public LuceneStandardTokenizer(String name) {
        super(name);

        this.v1Tokenizer = null;
        this.v2tokenizer = new LuceneStandardTokenizerV2(name);
    }

    /**
     * Get the maxTokenLength property: The maximum token length. Default is
     * 255. Tokens longer than the maximum length are split.
     *
     * @return the maxTokenLength value.
     */
    public Integer getMaxTokenLength() {
        return (v1Tokenizer != null) ? v1Tokenizer.getMaxTokenLength() : v2tokenizer.getMaxTokenLength();
    }

    /**
     * Set the maxTokenLength property: The maximum token length. Default is
     * 255. Tokens longer than the maximum length are split.
     *
     * @param maxTokenLength the maxTokenLength value to set.
     * @return the LuceneStandardTokenizer object itself.
     */
    public LuceneStandardTokenizer setMaxTokenLength(Integer maxTokenLength) {
        if (v1Tokenizer != null) {
            v1Tokenizer.setMaxTokenLength(maxTokenLength);
        } else {
            v2tokenizer.setMaxTokenLength(maxTokenLength);
        }
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return (v1Tokenizer != null) ? v1Tokenizer.toJson(jsonWriter) : v2tokenizer.toJson(jsonWriter);
    }
}
