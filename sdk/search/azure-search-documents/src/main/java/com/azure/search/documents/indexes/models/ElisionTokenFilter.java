// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;
import java.util.List;

/**
 * Removes elisions. For example, "l'avion" (the plane) will be converted to "avion" (plane). This token filter is
 * implemented using Apache Lucene.
 */
@Fluent
public final class ElisionTokenFilter extends TokenFilter {
    /*
     * The set of articles to remove.
     */
    private List<String> articles;

    /**
     * Constructor of {@link TokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits, spaces, dashes or underscores,
     * can only start and end with alphanumeric characters, and is limited to 128 characters.
     */
    public ElisionTokenFilter(String name) {
        super(name);
    }

    /**
     * Get the articles property: The set of articles to remove.
     *
     * @return the articles value.
     */
    public List<String> getArticles() {
        return this.articles;
    }

    /**
     * Set the articles property: The set of articles to remove.
     *
     * @param articles the articles value to set.
     * @return the ElisionTokenFilter object itself.
     */
    public ElisionTokenFilter setArticles(String... articles) {
        this.articles = (articles == null) ? null : Arrays.asList(articles);
        return this;
    }

    /**
     * Set the articles property: The set of articles to remove.
     *
     * @param articles the articles value to set.
     * @return the ElisionTokenFilter object itself.
     */
    public ElisionTokenFilter setArticles(List<String> articles) {
        this.articles = articles;
        return this;
    }
}
