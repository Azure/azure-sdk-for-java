/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

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
     * A Boolean value that indicates whether Bing requires the user's location
     * to provide accurate results. If you specified the user's location by
     * using the X-MSEdge-ClientIP and X-Search-Location headers, you can
     * ignore this field. For location aware queries, such as "today's weather"
     * or "restaurants near me" that need the user's location to provide
     * accurate results, this field is set to true. For location aware queries
     * that include the location (for example, "Seattle weather"), this field
     * is set to false. This field is also set to false for queries that are
     * not location aware, such as "best sellers".
     */
    @JsonProperty(value = "askUserForLocation", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean askUserForLocation;

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

    /**
     * Get the askUserForLocation value.
     *
     * @return the askUserForLocation value
     */
    public Boolean askUserForLocation() {
        return this.askUserForLocation;
    }

}
