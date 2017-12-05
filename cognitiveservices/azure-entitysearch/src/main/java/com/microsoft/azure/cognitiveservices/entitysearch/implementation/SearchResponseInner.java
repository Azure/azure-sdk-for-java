/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch.implementation;

import com.microsoft.azure.cognitiveservices.entitysearch.QueryContext;
import com.microsoft.azure.cognitiveservices.entitysearch.Entities;
import com.microsoft.azure.cognitiveservices.entitysearch.Places;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.entitysearch.Response;

/**
 * Defines the top-level object that the response includes when the request
 * succeeds.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("SearchResponse")
public class SearchResponseInner extends Response {
    /**
     * An object that contains the query string that Bing used for the request.
     * This object contains the query string as entered by the user. It may
     * also contain an altered query string that Bing used for the query if the
     * query string contained a spelling mistake.
     */
    @JsonProperty(value = "queryContext", access = JsonProperty.Access.WRITE_ONLY)
    private QueryContext queryContext;

    /**
     * A list of entities that are relevant to the search query.
     */
    @JsonProperty(value = "entities", access = JsonProperty.Access.WRITE_ONLY)
    private Entities entities;

    /**
     * A list of local entities such as restaurants or hotels that are relevant
     * to the query.
     */
    @JsonProperty(value = "places", access = JsonProperty.Access.WRITE_ONLY)
    private Places places;

    /**
     * Get the queryContext value.
     *
     * @return the queryContext value
     */
    public QueryContext queryContext() {
        return this.queryContext;
    }

    /**
     * Get the entities value.
     *
     * @return the entities value
     */
    public Entities entities() {
        return this.entities;
    }

    /**
     * Get the places value.
     *
     * @return the places value
     */
    public Places places() {
        return this.places;
    }

}
