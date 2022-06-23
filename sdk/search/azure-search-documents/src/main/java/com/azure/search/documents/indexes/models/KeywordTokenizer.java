// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV1;
import com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2;

/**
 * Emits the entire input as a single token. This tokenizer is implemented
 * using Apache Lucene.
 */
@Fluent
public final class KeywordTokenizer extends LexicalTokenizer {
    private final KeywordTokenizerV1 v1Tokenizer;
    private final KeywordTokenizerV2 v2Tokenizer;

    KeywordTokenizer(KeywordTokenizerV1 v1Tokenizer) {
        super(v1Tokenizer.getName());

        this.v1Tokenizer = v1Tokenizer;
        this.v2Tokenizer = null;
    }

    KeywordTokenizer(KeywordTokenizerV2 v2Tokenizer) {
        super(v2Tokenizer.getName());

        this.v1Tokenizer = null;
        this.v2Tokenizer = v2Tokenizer;
    }

    /**
     * Constructor of {@link KeywordTokenizer}.
     *
     * @param name The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public KeywordTokenizer(String name) {
        super(name);

        this.v1Tokenizer = null;
        this.v2Tokenizer = new KeywordTokenizerV2(name);
    }
    /**
     * Get the maxTokenLength property: The maximum token length. Default is
     * 256. Tokens longer than the maximum length are split. The maximum token
     * length that can be used is 300 characters.
     *
     * @return the maxTokenLength value.
     */
    public Integer getMaxTokenLength() {
        return (v1Tokenizer != null) ? v1Tokenizer.getBufferSize() : v2Tokenizer.getMaxTokenLength();
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
        if (v1Tokenizer != null) {
            v1Tokenizer.setBufferSize(maxTokenLength);
        } else {
            v2Tokenizer.setMaxTokenLength(maxTokenLength);
        }
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return (v1Tokenizer != null) ? v1Tokenizer.toJson(jsonWriter) : v2Tokenizer.toJson(jsonWriter);
    }
}
