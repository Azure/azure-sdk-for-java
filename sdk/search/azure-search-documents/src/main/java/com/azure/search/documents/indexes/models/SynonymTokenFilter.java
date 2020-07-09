// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * Matches single or multi-word synonyms in a token stream. This token filter
 * is implemented using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.SynonymTokenFilter")
@Fluent
public final class SynonymTokenFilter extends TokenFilter {
    /*
     * A list of synonyms in following one of two formats: 1. incredible,
     * unbelievable, fabulous => amazing - all terms on the left side of =>
     * symbol will be replaced with all terms on its right side; 2. incredible,
     * unbelievable, fabulous, amazing - comma separated list of equivalent
     * words. Set the expand option to change how this list is interpreted.
     */
    @JsonProperty(value = "synonyms", required = true)
    private List<String> synonyms;

    /*
     * A value indicating whether to case-fold input for matching. Default is
     * false.
     */
    @JsonProperty(value = "ignoreCase")
    private Boolean caseIgnored;

    /*
     * A value indicating whether all words in the list of synonyms (if =>
     * notation is not used) will map to one another. If true, all words in the
     * list of synonyms (if => notation is not used) will map to one another.
     * The following list: incredible, unbelievable, fabulous, amazing is
     * equivalent to: incredible, unbelievable, fabulous, amazing =>
     * incredible, unbelievable, fabulous, amazing. If false, the following
     * list: incredible, unbelievable, fabulous, amazing will be equivalent to:
     * incredible, unbelievable, fabulous, amazing => incredible. Default is
     * true.
     */
    @JsonProperty(value = "expand")
    private Boolean expand;

    /**
     * Constructor of {@link TokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     * @param synonyms A list of synonyms in following one of two formats:
     * <ul>
     * <li>incredible, unbelievable, fabulous =%3E amazing - all terms on the left side of =%3E
     * symbol will be replaced with all terms on its right side;</li>
     * <li>incredible, unbelievable, fabulous, amazing - comma separated list of equivalent
     * words. Set the expand option to change how this list is interpreted.</li>
     * </ul>
     */
    public SynonymTokenFilter(String name, List<String> synonyms) {
        super(name);
        this.synonyms = synonyms;
    }

    /**
     * Get the synonyms property: A list of synonyms in following one of two
     * formats: 1. incredible, unbelievable, fabulous =&gt; amazing - all terms
     * on the left side of =&gt; symbol will be replaced with all terms on its
     * right side; 2. incredible, unbelievable, fabulous, amazing - comma
     * separated list of equivalent words. Set the expand option to change how
     * this list is interpreted.
     *
     * @return the synonyms value.
     */
    public List<String> getSynonyms() {
        return this.synonyms;
    }

    /**
     * Get the ignoreCase property: A value indicating whether to case-fold
     * input for matching. Default is false.
     *
     * @return the ignoreCase value.
     */
    public Boolean isCaseIgnored() {
        return this.caseIgnored;
    }

    /**
     * Set the ignoreCase property: A value indicating whether to case-fold
     * input for matching. Default is false.
     *
     * @param caseIgnored the ignoreCase value to set.
     * @return the SynonymTokenFilter object itself.
     */
    public SynonymTokenFilter setCaseIgnored(Boolean caseIgnored) {
        this.caseIgnored = caseIgnored;
        return this;
    }

    /**
     * Get the expand property: A value indicating whether all words in the
     * list of synonyms (if =&gt; notation is not used) will map to one
     * another. If true, all words in the list of synonyms (if =&gt; notation
     * is not used) will map to one another. The following list: incredible,
     * unbelievable, fabulous, amazing is equivalent to: incredible,
     * unbelievable, fabulous, amazing =&gt; incredible, unbelievable,
     * fabulous, amazing. If false, the following list: incredible,
     * unbelievable, fabulous, amazing will be equivalent to: incredible,
     * unbelievable, fabulous, amazing =&gt; incredible. Default is true.
     *
     * @return the expand value.
     */
    public Boolean getExpand() {
        return this.expand;
    }

    /**
     * Set the expand property: A value indicating whether all words in the
     * list of synonyms (if =&gt; notation is not used) will map to one
     * another. If true, all words in the list of synonyms (if =&gt; notation
     * is not used) will map to one another. The following list: incredible,
     * unbelievable, fabulous, amazing is equivalent to: incredible,
     * unbelievable, fabulous, amazing =&gt; incredible, unbelievable,
     * fabulous, amazing. If false, the following list: incredible,
     * unbelievable, fabulous, amazing will be equivalent to: incredible,
     * unbelievable, fabulous, amazing =&gt; incredible. Default is true.
     *
     * @param expand the expand value to set.
     * @return the SynonymTokenFilter object itself.
     */
    public SynonymTokenFilter setExpand(Boolean expand) {
        this.expand = expand;
        return this;
    }
}
