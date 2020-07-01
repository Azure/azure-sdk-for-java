// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Divides text using language-specific rules and reduces words to their base
 * forms.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.MicrosoftLanguageStemmingTokenizer")
@Fluent
public final class MicrosoftLanguageStemmingTokenizer extends LexicalTokenizer {
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
    private Boolean isSearchTokenizerUsed;

    /*
     * The language to use. The default is English. Possible values include:
     * 'Arabic', 'Bangla', 'Bulgarian', 'Catalan', 'Croatian', 'Czech',
     * 'Danish', 'Dutch', 'English', 'Estonian', 'Finnish', 'French', 'German',
     * 'Greek', 'Gujarati', 'Hebrew', 'Hindi', 'Hungarian', 'Icelandic',
     * 'Indonesian', 'Italian', 'Kannada', 'Latvian', 'Lithuanian', 'Malay',
     * 'Malayalam', 'Marathi', 'NorwegianBokmaal', 'Polish', 'Portuguese',
     * 'PortugueseBrazilian', 'Punjabi', 'Romanian', 'Russian',
     * 'SerbianCyrillic', 'SerbianLatin', 'Slovak', 'Slovenian', 'Spanish',
     * 'Swedish', 'Tamil', 'Telugu', 'Turkish', 'Ukrainian', 'Urdu'
     */
    @JsonProperty(value = "language")
    private MicrosoftStemmingTokenizerLanguage language;

    /**
     * Constructor of {@link MicrosoftLanguageStemmingTokenizer}.
     *
     * @param name The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public MicrosoftLanguageStemmingTokenizer(String name) {
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
     * @return the MicrosoftLanguageStemmingTokenizer object itself.
     */
    public MicrosoftLanguageStemmingTokenizer setMaxTokenLength(Integer maxTokenLength) {
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
        return this.isSearchTokenizerUsed;
    }

    /**
     * Set the isSearchTokenizer property: A value indicating how the tokenizer
     * is used. Set to true if used as the search tokenizer, set to false if
     * used as the indexing tokenizer. Default is false.
     *
     * @param isSearchTokenizerUsed the isSearchTokenizer value to set.
     * @return the MicrosoftLanguageStemmingTokenizer object itself.
     */
    public MicrosoftLanguageStemmingTokenizer setIsSearchTokenizerUsed(Boolean isSearchTokenizerUsed) {
        this.isSearchTokenizerUsed = isSearchTokenizerUsed;
        return this;
    }

    /**
     * Get the language property: The language to use. The default is English.
     * Possible values include: 'Arabic', 'Bangla', 'Bulgarian', 'Catalan',
     * 'Croatian', 'Czech', 'Danish', 'Dutch', 'English', 'Estonian',
     * 'Finnish', 'French', 'German', 'Greek', 'Gujarati', 'Hebrew', 'Hindi',
     * 'Hungarian', 'Icelandic', 'Indonesian', 'Italian', 'Kannada', 'Latvian',
     * 'Lithuanian', 'Malay', 'Malayalam', 'Marathi', 'NorwegianBokmaal',
     * 'Polish', 'Portuguese', 'PortugueseBrazilian', 'Punjabi', 'Romanian',
     * 'Russian', 'SerbianCyrillic', 'SerbianLatin', 'Slovak', 'Slovenian',
     * 'Spanish', 'Swedish', 'Tamil', 'Telugu', 'Turkish', 'Ukrainian', 'Urdu'.
     *
     * @return the language value.
     */
    public MicrosoftStemmingTokenizerLanguage getLanguage() {
        return this.language;
    }

    /**
     * Set the language property: The language to use. The default is English.
     * Possible values include: 'Arabic', 'Bangla', 'Bulgarian', 'Catalan',
     * 'Croatian', 'Czech', 'Danish', 'Dutch', 'English', 'Estonian',
     * 'Finnish', 'French', 'German', 'Greek', 'Gujarati', 'Hebrew', 'Hindi',
     * 'Hungarian', 'Icelandic', 'Indonesian', 'Italian', 'Kannada', 'Latvian',
     * 'Lithuanian', 'Malay', 'Malayalam', 'Marathi', 'NorwegianBokmaal',
     * 'Polish', 'Portuguese', 'PortugueseBrazilian', 'Punjabi', 'Romanian',
     * 'Russian', 'SerbianCyrillic', 'SerbianLatin', 'Slovak', 'Slovenian',
     * 'Spanish', 'Swedish', 'Tamil', 'Telugu', 'Turkish', 'Ukrainian', 'Urdu'.
     *
     * @param language the language value to set.
     * @return the MicrosoftLanguageStemmingTokenizer object itself.
     */
    public MicrosoftLanguageStemmingTokenizer setLanguage(MicrosoftStemmingTokenizerLanguage language) {
        this.language = language;
        return this;
    }
}
