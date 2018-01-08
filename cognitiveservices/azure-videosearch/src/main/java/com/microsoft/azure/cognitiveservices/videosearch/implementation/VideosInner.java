/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.videosearch.VideoObject;
import com.microsoft.azure.cognitiveservices.videosearch.VideoQueryScenario;
import com.microsoft.azure.cognitiveservices.videosearch.Query;
import com.microsoft.azure.cognitiveservices.videosearch.PivotSuggestions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.videosearch.SearchResultsAnswer;

/**
 * Defines a video answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = VideosInner.class)
@JsonTypeName("Videos")
public class VideosInner extends SearchResultsAnswer {
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
     * Possible values include: 'List', 'SingleDominantVideo'.
     */
    @JsonProperty(value = "scenario", access = JsonProperty.Access.WRITE_ONLY)
    private VideoQueryScenario scenario;

    /**
     * The queryExpansions property.
     */
    @JsonProperty(value = "queryExpansions", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> queryExpansions;

    /**
     * The pivotSuggestions property.
     */
    @JsonProperty(value = "pivotSuggestions", access = JsonProperty.Access.WRITE_ONLY)
    private List<PivotSuggestions> pivotSuggestions;

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
     * @return the VideosInner object itself.
     */
    public VideosInner withValue(List<VideoObject> value) {
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
     * Get the scenario value.
     *
     * @return the scenario value
     */
    public VideoQueryScenario scenario() {
        return this.scenario;
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

}
