/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a video answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Videos.class)
@JsonTypeName("Videos")
public class Videos extends SearchResultsAnswer {
    /**
     * A list of video objects that are relevant to the query.
     */
    @JsonProperty(value = "value", required = true)
    private List<VideoObject> value;

    /**
     * The nextOffset property.
     */
    @JsonProperty(value = "nextOffset", access = JsonProperty.Access.WRITE_ONLY)
    private Integer nextOffset;

    /**
     * The queryExpansions property.
     */
    @JsonProperty(value = "queryExpansions", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> queryExpansions;

    /**
     * The relatedSearches property.
     */
    @JsonProperty(value = "relatedSearches", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> relatedSearches;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<VideoObject> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the Videos object itself.
     */
    public Videos withValue(List<VideoObject> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextOffset value.
     *
     * @return the nextOffset value
     */
    public Integer nextOffset() {
        return this.nextOffset;
    }

    /**
     * Get the queryExpansions value.
     *
     * @return the queryExpansions value
     */
    public List<Query> queryExpansions() {
        return this.queryExpansions;
    }

    /**
     * Get the relatedSearches value.
     *
     * @return the relatedSearches value
     */
    public List<Query> relatedSearches() {
        return this.relatedSearches;
    }

}
