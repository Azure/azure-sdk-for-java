// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Base type for tokenizers.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = LexicalTokenizer.class)
@JsonTypeName("LexicalTokenizer")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.ClassicTokenizer", value = ClassicTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.EdgeNGramTokenizer", value = EdgeNGramTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.KeywordTokenizer", value = KeywordTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.KeywordTokenizerV2", value = KeywordTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.MicrosoftLanguageTokenizer",
        value = MicrosoftLanguageTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.MicrosoftLanguageStemmingTokenizer",
        value = MicrosoftLanguageStemmingTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.NGramTokenizer", value = NGramTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.PathHierarchyTokenizerV2",
        value = PathHierarchyTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.PatternTokenizer", value = PatternTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.StandardTokenizer", value = LuceneStandardTokenizer.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.UaxUrlEmailTokenizer", value = UaxUrlEmailTokenizer.class)
})
@Fluent
public abstract class LexicalTokenizer {
    /*
     * The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /**
     * Constructor of {@link LexicalTokenizer}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @JsonCreator
    public LexicalTokenizer(@JsonProperty(value = "name") String name) {
        this.name = name;
    }

    /**
     * Get the name property: The name of the tokenizer. It must only contain
     * letters, digits, spaces, dashes or underscores, can only start and end
     * with alphanumeric characters, and is limited to 128 characters.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

}
