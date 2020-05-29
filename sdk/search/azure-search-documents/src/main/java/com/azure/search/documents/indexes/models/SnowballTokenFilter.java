// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A filter that stems words using a Snowball-generated stemmer. This token
 * filter is implemented using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.SnowballTokenFilter")
@Fluent
public final class SnowballTokenFilter extends TokenFilter {
    /*
     * The language to use. Possible values include: 'Armenian', 'Basque',
     * 'Catalan', 'Danish', 'Dutch', 'English', 'Finnish', 'French', 'German',
     * 'German2', 'Hungarian', 'Italian', 'Kp', 'Lovins', 'Norwegian',
     * 'Porter', 'Portuguese', 'Romanian', 'Russian', 'Spanish', 'Swedish',
     * 'Turkish'
     */
    @JsonProperty(value = "language", required = true)
    private SnowballTokenFilterLanguage language;

    /**
     * Get the language property: The language to use. Possible values include:
     * 'Armenian', 'Basque', 'Catalan', 'Danish', 'Dutch', 'English',
     * 'Finnish', 'French', 'German', 'German2', 'Hungarian', 'Italian', 'Kp',
     * 'Lovins', 'Norwegian', 'Porter', 'Portuguese', 'Romanian', 'Russian',
     * 'Spanish', 'Swedish', 'Turkish'.
     *
     * @return the language value.
     */
    public SnowballTokenFilterLanguage getLanguage() {
        return this.language;
    }

    /**
     * Set the language property: The language to use. Possible values include:
     * 'Armenian', 'Basque', 'Catalan', 'Danish', 'Dutch', 'English',
     * 'Finnish', 'French', 'German', 'German2', 'Hungarian', 'Italian', 'Kp',
     * 'Lovins', 'Norwegian', 'Porter', 'Portuguese', 'Romanian', 'Russian',
     * 'Spanish', 'Swedish', 'Turkish'.
     *
     * @param language the language value to set.
     * @return the SnowballTokenFilter object itself.
     */
    public SnowballTokenFilter setLanguage(SnowballTokenFilterLanguage language) {
        this.language = language;
        return this;
    }
}
