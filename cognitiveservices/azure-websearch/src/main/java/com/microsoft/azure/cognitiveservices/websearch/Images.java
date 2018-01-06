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
 * Defines an image answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Images.class)
@JsonTypeName("Images")
public class Images extends SearchResultsAnswer {
    /**
     * The nextOffset property.
     */
    @JsonProperty(value = "nextOffset", access = JsonProperty.Access.WRITE_ONLY)
    private Integer nextOffset;

    /**
     * A list of image objects that are relevant to the query. If there are no
     * results, the List is empty.
     */
    @JsonProperty(value = "value", required = true)
    private List<ImageObject> value;

    /**
     * The queryExpansions property.
     */
    @JsonProperty(value = "queryExpansions", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> queryExpansions;

    /**
     * The similarTerms property.
     */
    @JsonProperty(value = "similarTerms", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> similarTerms;

    /**
     * The relatedSearches property.
     */
    @JsonProperty(value = "relatedSearches", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> relatedSearches;

    /**
     * Get the nextOffset value.
     *
     * @return the nextOffset value
     */
    public Integer nextOffset() {
        return this.nextOffset;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<ImageObject> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the Images object itself.
     */
    public Images withValue(List<ImageObject> value) {
        this.value = value;
        return this;
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
     * Get the similarTerms value.
     *
     * @return the similarTerms value
     */
    public List<Query> similarTerms() {
        return this.similarTerms;
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
