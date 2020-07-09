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
 * Base type for token filters.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = TokenFilter.class)
@JsonTypeName("TokenFilter")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.AsciiFoldingTokenFilter", value = AsciiFoldingTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.CjkBigramTokenFilter", value = CjkBigramTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.CommonGramTokenFilter", value = CommonGramTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.DictionaryDecompounderTokenFilter",
        value = DictionaryDecompounderTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.EdgeNGramTokenFilter", value = EdgeNGramTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.ElisionTokenFilter", value = ElisionTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.KeepTokenFilter", value = KeepTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.KeywordMarkerTokenFilter",
        value = KeywordMarkerTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.LengthTokenFilter", value = LengthTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.LimitTokenFilter", value = LimitTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.NGramTokenFilter", value = NGramTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.PatternCaptureTokenFilter",
        value = PatternCaptureTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.PatternReplaceTokenFilter",
        value = PatternReplaceTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.PhoneticTokenFilter", value = PhoneticTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.ShingleTokenFilter", value = ShingleTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.SnowballTokenFilter", value = SnowballTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.StemmerTokenFilter", value = StemmerTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.StemmerOverrideTokenFilter",
        value = StemmerOverrideTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.StopwordsTokenFilter", value = StopwordsTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.SynonymTokenFilter", value = SynonymTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.TruncateTokenFilter", value = TruncateTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.UniqueTokenFilter", value = UniqueTokenFilter.class),
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.WordDelimiterTokenFilter",
        value = WordDelimiterTokenFilter.class)
})
@Fluent
public abstract class TokenFilter {
    /*
     * The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /**
     * Constructor of {@link TokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @JsonCreator
    public TokenFilter(@JsonProperty(value = "name", required = true) String name) {
        this.name = name;
    }

    /**
     * Get the name property: The name of the token filter. It must only
     * contain letters, digits, spaces, dashes or underscores, can only start
     * and end with alphanumeric characters, and is limited to 128 characters.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

}
