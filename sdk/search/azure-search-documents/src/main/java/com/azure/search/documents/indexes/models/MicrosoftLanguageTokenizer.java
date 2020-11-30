// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Divides text using language-specific rules.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.MicrosoftLanguageTokenizer")
@Fluent
public final class MicrosoftLanguageTokenizer extends LexicalTokenizer {
    /*
     * The maximum token length. Tokens longer than the maximum length are
     * split. Maximum token length that can be used is 300 characters. Tokens
     * longer than 300 characters are first split into tokens of length 300 and
     * then each of those tokens is split based on the max token length set.
     * Default is 255.
     */
    @JsonProperty(value = "maxTokenLength")
    private Integer maxTokenLength;

    /*
     * A value indicating how the tokenizer is used. Set to true if used as the
     * search tokenizer, set to false if used as the indexing tokenizer.
     * Default is false.
     */
    @JsonProperty(value = "isSearchTokenizer")
    private Boolean isSearchTokenizer;

    /*
     * The language to use. The default is English. Possible values include:
     * 'Bangla', 'Bulgarian', 'Catalan', 'ChineseSimplified',
     * 'ChineseTraditional', 'Croatian', 'Czech', 'Danish', 'Dutch', 'English',
     * 'French', 'German', 'Greek', 'Gujarati', 'Hindi', 'Icelandic',
     * 'Indonesian', 'Italian', 'Japanese', 'Kannada', 'Korean', 'Malay',
     * 'Malayalam', 'Marathi', 'NorwegianBokmaal', 'Polish', 'Portuguese',
     * 'PortugueseBrazilian', 'Punjabi', 'Romanian', 'Russian',
     * 'SerbianCyrillic', 'SerbianLatin', 'Slovenian', 'Spanish', 'Swedish',
     * 'Tamil', 'Telugu', 'Thai', 'Ukrainian', 'Urdu', 'Vietnamese'
     */
    @JsonProperty(value = "language")
    private MicrosoftTokenizerLanguage language;

    /**
     * Constructor of {@link MicrosoftLanguageTokenizer}.
     *
     * @param name The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public MicrosoftLanguageTokenizer(String name) {
        super(name);
    }

    /**
     * Get the maxTokenLength property: The maximum token length. Tokens longer
     * than the maximum length are split. Maximum token length that can be used
     * is 300 characters. Tokens longer than 300 characters are first split
     * into tokens of length 300 and then each of those tokens is split based
     * on the max token length set. Default is 255.
     *
     * @return the maxTokenLength value.
     */
    public Integer getMaxTokenLength() {
        return this.maxTokenLength;
    }

    /**
     * Set the maxTokenLength property: The maximum token length. Tokens longer
     * than the maximum length are split. Maximum token length that can be used
     * is 300 characters. Tokens longer than 300 characters are first split
     * into tokens of length 300 and then each of those tokens is split based
     * on the max token length set. Default is 255.
     *
     * @param maxTokenLength the maxTokenLength value to set.
     * @return the MicrosoftLanguageTokenizer object itself.
     */
    public MicrosoftLanguageTokenizer setMaxTokenLength(Integer maxTokenLength) {
        this.maxTokenLength = maxTokenLength;
        return this;
    }

    /**
     * Get the isSearchTokenizer property: A value indicating how the tokenizer
     * is used. Set to true if used as the search tokenizer, set to false if
     * used as the indexing tokenizer. Default is false.
     *
     * @return the isSearchTokenizer value.
     */
    public Boolean isSearchTokenizer() {
        return this.isSearchTokenizer;
    }

    /**
     * Set the isSearchTokenizer property: A value indicating how the tokenizer
     * is used. Set to true if used as the search tokenizer, set to false if
     * used as the indexing tokenizer. Default is false.
     *
     * @param isSearchTokenizer the isSearchTokenizer value to set.
     * @return the MicrosoftLanguageTokenizer object itself.
     */
    public MicrosoftLanguageTokenizer setIsSearchTokenizer(Boolean isSearchTokenizer) {
        this.isSearchTokenizer = isSearchTokenizer;
        return this;
    }

    /**
     * Get the language property: The language to use. The default is English.
     * Possible values include: 'Bangla', 'Bulgarian', 'Catalan',
     * 'ChineseSimplified', 'ChineseTraditional', 'Croatian', 'Czech',
     * 'Danish', 'Dutch', 'English', 'French', 'German', 'Greek', 'Gujarati',
     * 'Hindi', 'Icelandic', 'Indonesian', 'Italian', 'Japanese', 'Kannada',
     * 'Korean', 'Malay', 'Malayalam', 'Marathi', 'NorwegianBokmaal', 'Polish',
     * 'Portuguese', 'PortugueseBrazilian', 'Punjabi', 'Romanian', 'Russian',
     * 'SerbianCyrillic', 'SerbianLatin', 'Slovenian', 'Spanish', 'Swedish',
     * 'Tamil', 'Telugu', 'Thai', 'Ukrainian', 'Urdu', 'Vietnamese'.
     *
     * @return the language value.
     */
    public MicrosoftTokenizerLanguage getLanguage() {
        return this.language;
    }

    /**
     * Set the language property: The language to use. The default is English.
     * Possible values include: 'Bangla', 'Bulgarian', 'Catalan',
     * 'ChineseSimplified', 'ChineseTraditional', 'Croatian', 'Czech',
     * 'Danish', 'Dutch', 'English', 'French', 'German', 'Greek', 'Gujarati',
     * 'Hindi', 'Icelandic', 'Indonesian', 'Italian', 'Japanese', 'Kannada',
     * 'Korean', 'Malay', 'Malayalam', 'Marathi', 'NorwegianBokmaal', 'Polish',
     * 'Portuguese', 'PortugueseBrazilian', 'Punjabi', 'Romanian', 'Russian',
     * 'SerbianCyrillic', 'SerbianLatin', 'Slovenian', 'Spanish', 'Swedish',
     * 'Tamil', 'Telugu', 'Thai', 'Ukrainian', 'Urdu', 'Vietnamese'.
     *
     * @param language the language value to set.
     * @return the MicrosoftLanguageTokenizer object itself.
     */
    public MicrosoftLanguageTokenizer setLanguage(MicrosoftTokenizerLanguage language) {
        this.language = language;
        return this;
    }
}
