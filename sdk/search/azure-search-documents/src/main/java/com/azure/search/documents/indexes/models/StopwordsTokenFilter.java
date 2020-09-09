// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Arrays;
import java.util.List;

/**
 * Removes stop words from a token stream. This token filter is implemented
 * using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.StopwordsTokenFilter")
@Fluent
public final class StopwordsTokenFilter extends TokenFilter {
    /*
     * The list of stopwords. This property and the stopwords list property
     * cannot both be set.
     */
    @JsonProperty(value = "stopwords")
    private List<String> stopwords;

    /*
     * A predefined list of stopwords to use. This property and the stopwords
     * property cannot both be set. Default is English. Possible values
     * include: 'Arabic', 'Armenian', 'Basque', 'Brazilian', 'Bulgarian',
     * 'Catalan', 'Czech', 'Danish', 'Dutch', 'English', 'Finnish', 'French',
     * 'Galician', 'German', 'Greek', 'Hindi', 'Hungarian', 'Indonesian',
     * 'Irish', 'Italian', 'Latvian', 'Norwegian', 'Persian', 'Portuguese',
     * 'Romanian', 'Russian', 'Sorani', 'Spanish', 'Swedish', 'Thai', 'Turkish'
     */
    @JsonProperty(value = "stopwordsList")
    private StopwordsList stopwordsList;

    /*
     * A value indicating whether to ignore case. If true, all words are
     * converted to lower case first. Default is false.
     */
    @JsonProperty(value = "ignoreCase")
    private Boolean caseIgnored;

    /*
     * A value indicating whether to ignore the last search term if it's a stop
     * word. Default is true.
     */
    @JsonProperty(value = "removeTrailing")
    private Boolean trailingStopWordsRemoved;

    /**
     * Constructor of {@link StopwordsTokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public StopwordsTokenFilter(String name) {
        super(name);
    }

    /**
     * Get the stopwords property: The list of stopwords. This property and the
     * stopwords list property cannot both be set.
     *
     * @return the stopwords value.
     */
    public List<String> getStopwords() {
        return this.stopwords;
    }

    /**
     * Set the stopwords property: The list of stopwords. This property and the
     * stopwords list property cannot both be set.
     *
     * @param stopwords the stopwords value to set.
     * @return the StopwordsTokenFilter object itself.
     */
    public StopwordsTokenFilter setStopwords(String... stopwords) {
        this.stopwords = (stopwords == null) ? null : Arrays.asList(stopwords);
        return this;
    }

    /**
     * Set the stopwords property: The list of stopwords. This property and the
     * stopwords list property cannot both be set.
     *
     * @param stopwords the stopwords value to set.
     * @return the StopwordsTokenFilter object itself.
     */
    @JsonSetter
    public StopwordsTokenFilter setStopwords(List<String> stopwords) {
        this.stopwords = stopwords;
        return this;
    }

    /**
     * Get the stopwordsList property: A predefined list of stopwords to use.
     * This property and the stopwords property cannot both be set. Default is
     * English. Possible values include: 'Arabic', 'Armenian', 'Basque',
     * 'Brazilian', 'Bulgarian', 'Catalan', 'Czech', 'Danish', 'Dutch',
     * 'English', 'Finnish', 'French', 'Galician', 'German', 'Greek', 'Hindi',
     * 'Hungarian', 'Indonesian', 'Irish', 'Italian', 'Latvian', 'Norwegian',
     * 'Persian', 'Portuguese', 'Romanian', 'Russian', 'Sorani', 'Spanish',
     * 'Swedish', 'Thai', 'Turkish'.
     *
     * @return the stopwordsList value.
     */
    public StopwordsList getStopwordsList() {
        return this.stopwordsList;
    }

    /**
     * Set the stopwordsList property: A predefined list of stopwords to use.
     * This property and the stopwords property cannot both be set. Default is
     * English. Possible values include: 'Arabic', 'Armenian', 'Basque',
     * 'Brazilian', 'Bulgarian', 'Catalan', 'Czech', 'Danish', 'Dutch',
     * 'English', 'Finnish', 'French', 'Galician', 'German', 'Greek', 'Hindi',
     * 'Hungarian', 'Indonesian', 'Irish', 'Italian', 'Latvian', 'Norwegian',
     * 'Persian', 'Portuguese', 'Romanian', 'Russian', 'Sorani', 'Spanish',
     * 'Swedish', 'Thai', 'Turkish'.
     *
     * @param stopwordsList the stopwordsList value to set.
     * @return the StopwordsTokenFilter object itself.
     */
    public StopwordsTokenFilter setStopwordsList(StopwordsList stopwordsList) {
        this.stopwordsList = stopwordsList;
        return this;
    }

    /**
     * Get the ignoreCase property: A value indicating whether to ignore case.
     * If true, all words are converted to lower case first. Default is false.
     *
     * @return the ignoreCase value.
     */
    public Boolean isCaseIgnored() {
        return this.caseIgnored;
    }

    /**
     * Set the ignoreCase property: A value indicating whether to ignore case.
     * If true, all words are converted to lower case first. Default is false.
     *
     * @param caseIgnored the ignoreCase value to set.
     * @return the StopwordsTokenFilter object itself.
     */
    public StopwordsTokenFilter setCaseIgnored(Boolean caseIgnored) {
        this.caseIgnored = caseIgnored;
        return this;
    }

    /**
     * Get the removeTrailingStopWords property: A value indicating whether to
     * ignore the last search term if it's a stop word. Default is true.
     *
     * @return the removeTrailingStopWords value.
     */
    public Boolean areTrailingStopWordsRemoved() {
        return this.trailingStopWordsRemoved;
    }

    /**
     * Set the removeTrailingStopWords property: A value indicating whether to
     * ignore the last search term if it's a stop word. Default is true.
     *
     * @param trailingStopWordsRemoved the removeTrailingStopWords value to set.
     * @return the StopwordsTokenFilter object itself.
     */
    public StopwordsTokenFilter setTrailingStopWordsRemoved(Boolean trailingStopWordsRemoved) {
        this.trailingStopWordsRemoved = trailingStopWordsRemoved;
        return this;
    }
}
