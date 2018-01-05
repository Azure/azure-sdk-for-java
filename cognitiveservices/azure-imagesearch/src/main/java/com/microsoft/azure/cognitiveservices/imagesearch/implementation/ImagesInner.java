/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.imagesearch.ImageObject;
import com.microsoft.azure.cognitiveservices.imagesearch.Query;
import com.microsoft.azure.cognitiveservices.imagesearch.PivotSuggestions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.imagesearch.SearchResultsAnswer;

/**
 * Defines an image answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = ImagesInner.class)
@JsonTypeName("Images")
public class ImagesInner extends SearchResultsAnswer {
    /**
     * Used as part of deduping. Tells client the next offset that client
     * should use in the next pagination request.
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
     * A list of expanded queries that narrows the original query. For example,
     * if the query was Microsoft Surface, the expanded queries might be:
     * Microsoft Surface Pro 3, Microsoft Surface RT, Microsoft Surface Phone,
     * and Microsoft Surface Hub.
     */
    @JsonProperty(value = "queryExpansions", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> queryExpansions;

    /**
     * A list of segments in the original query. For example, if the query was
     * Red Flowers, Bing might segment the query into Red and Flowers. The
     * Flowers pivot may contain query suggestions such as Red Peonies and Red
     * Daisies, and the Red pivot may contain query suggestions such as Green
     * Flowers and Yellow Flowers.
     */
    @JsonProperty(value = "pivotSuggestions", access = JsonProperty.Access.WRITE_ONLY)
    private List<PivotSuggestions> pivotSuggestions;

    /**
     * A list of terms that are similar in meaning to the user's query term.
     */
    @JsonProperty(value = "similarTerms", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> similarTerms;

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
     * @return the ImagesInner object itself.
     */
    public ImagesInner withValue(List<ImageObject> value) {
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
     * Get the pivotSuggestions value.
     *
     * @return the pivotSuggestions value
     */
    public List<PivotSuggestions> pivotSuggestions() {
        return this.pivotSuggestions;
    }

    /**
     * Get the similarTerms value.
     *
     * @return the similarTerms value
     */
    public List<Query> similarTerms() {
        return this.similarTerms;
    }

}
