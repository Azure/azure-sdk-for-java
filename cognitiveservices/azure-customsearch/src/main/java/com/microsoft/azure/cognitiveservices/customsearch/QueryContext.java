/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.customsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the query context that Bing used for the request.
 */
public class QueryContext {
    /**
     * The query string as specified in the request.
     */
    @JsonProperty(value = "originalQuery", required = true)
    private String originalQuery;

    /**
     * The query string used by Bing to perform the query. Bing uses the
     * altered query string if the original query string contained spelling
     * mistakes. For example, if the query string is "saling downwind", the
     * altered query string will be "sailing downwind". This field is included
     * only if the original query string contains a spelling mistake.
     */
    @JsonProperty(value = "alteredQuery", access = JsonProperty.Access.WRITE_ONLY)
    private String alteredQuery;

    /**
     * The query string to use to force Bing to use the original string. For
     * example, if the query string is "saling downwind", the override query
     * string will be "+saling downwind". Remember to encode the query string
     * which results in "%2Bsaling+downwind". This field is included only if
     * the original query string contains a spelling mistake.
     */
    @JsonProperty(value = "alterationOverrideQuery", access = JsonProperty.Access.WRITE_ONLY)
    private String alterationOverrideQuery;

    /**
     * A Boolean value that indicates whether the specified query has adult
     * intent. The value is true if the query has adult intent; otherwise,
     * false.
     */
    @JsonProperty(value = "adultIntent", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean adultIntent;

    /**
     * Get the originalQuery value.
     *
     * @return the originalQuery value
     */
    public String originalQuery() {
        return this.originalQuery;
    }

    /**
     * Set the originalQuery value.
     *
     * @param originalQuery the originalQuery value to set
     * @return the QueryContext object itself.
     */
    public QueryContext withOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
        return this;
    }

    /**
     * Get the alteredQuery value.
     *
     * @return the alteredQuery value
     */
    public String alteredQuery() {
        return this.alteredQuery;
    }

    /**
     * Get the alterationOverrideQuery value.
     *
     * @return the alterationOverrideQuery value
     */
    public String alterationOverrideQuery() {
        return this.alterationOverrideQuery;
    }

    /**
     * Get the adultIntent value.
     *
     * @return the adultIntent value
     */
    public Boolean adultIntent() {
        return this.adultIntent;
    }

}
