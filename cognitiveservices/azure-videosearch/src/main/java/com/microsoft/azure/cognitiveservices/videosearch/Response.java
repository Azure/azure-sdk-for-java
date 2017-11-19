/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.microsoft.azure.cognitiveservices.videosearch.implementation.TrendingVideosInner;
import com.microsoft.azure.cognitiveservices.videosearch.implementation.VideoDetailsInner;

/**
 * Defines a response. All schemas that could be returned at the root of a
 * response should inherit from this.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("Response")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Answer", value = Answer.class),
    @JsonSubTypes.Type(name = "Thing", value = Thing.class),
    @JsonSubTypes.Type(name = "ErrorResponse", value = ErrorResponse.class),
    @JsonSubTypes.Type(name = "TrendingVideos", value = TrendingVideosInner.class),
    @JsonSubTypes.Type(name = "VideoDetails", value = VideoDetailsInner.class)
})
public class Response extends Identifiable {
    /**
     * The URL To Bing's search result for this item.
     */
    @JsonProperty(value = "webSearchUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String webSearchUrl;

    /**
     * Get the webSearchUrl value.
     *
     * @return the webSearchUrl value
     */
    public String webSearchUrl() {
        return this.webSearchUrl;
    }

}
