/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.customsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a search query.
 */
public class Query {
    /**
     * The query string. Use this string as the query term in a new search
     * request.
     */
    @JsonProperty(value = "text", required = true)
    private String text;

    /**
     * The display version of the query term. This version of the query term
     * may contain special characters that highlight the search term found in
     * the query string. The string contains the highlighting characters only
     * if the query enabled hit highlighting.
     */
    @JsonProperty(value = "displayText", access = JsonProperty.Access.WRITE_ONLY)
    private String displayText;

    /**
     * The URL that takes the user to the Bing search results page for the
     * query.Only related search results include this field.
     */
    @JsonProperty(value = "webSearchUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String webSearchUrl;

    /**
     * The searchLink property.
     */
    @JsonProperty(value = "searchLink", access = JsonProperty.Access.WRITE_ONLY)
    private String searchLink;

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param text the text value to set
     * @return the Query object itself.
     */
    public Query withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the displayText value.
     *
     * @return the displayText value
     */
    public String displayText() {
        return this.displayText;
    }

    /**
     * Get the webSearchUrl value.
     *
     * @return the webSearchUrl value
     */
    public String webSearchUrl() {
        return this.webSearchUrl;
    }

    /**
     * Get the searchLink value.
     *
     * @return the searchLink value
     */
    public String searchLink() {
        return this.searchLink;
    }

}
