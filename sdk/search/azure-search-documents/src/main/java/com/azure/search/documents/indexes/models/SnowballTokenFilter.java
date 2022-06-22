// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;

/**
 * A filter that stems words using a Snowball-generated stemmer. This token
 * filter is implemented using Apache Lucene.
 */
@Fluent
public final class SnowballTokenFilter extends TokenFilter {
    /*
     * The language to use. Possible values include: 'Armenian', 'Basque',
     * 'Catalan', 'Danish', 'Dutch', 'English', 'Finnish', 'French', 'German',
     * 'German2', 'Hungarian', 'Italian', 'Kp', 'Lovins', 'Norwegian',
     * 'Porter', 'Portuguese', 'Romanian', 'Russian', 'Spanish', 'Swedish',
     * 'Turkish'
     */
    private final SnowballTokenFilterLanguage language;

    /**
     * Constructor of {@link TokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     * @param language The language to use. Possible values include: 'Armenian', 'Basque',
     * 'Catalan', 'Danish', 'Dutch', 'English', 'Finnish', 'French', 'German',
     * 'German2', 'Hungarian', 'Italian', 'Kp', 'Lovins', 'Norwegian',
     * 'Porter', 'Portuguese', 'Romanian', 'Russian', 'Spanish', 'Swedish',
     * 'Turkish'
     */
    public SnowballTokenFilter(String name, SnowballTokenFilterLanguage language) {
        super(name);
        this.language = language;
    }

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

}
