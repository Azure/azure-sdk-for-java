/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch.implementation;

import com.microsoft.azure.cognitiveservices.videosearch.VideosModule;
import com.microsoft.azure.cognitiveservices.videosearch.VideoObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.videosearch.Response;

/**
 * The VideoDetailsInner model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
    defaultImpl = VideoDetailsInner.class)
@JsonTypeName("VideoDetails")
public class VideoDetailsInner extends Response {
    /**
     * The relatedVideos property.
     */
    @JsonProperty(value = "relatedVideos", access = JsonProperty.Access.WRITE_ONLY)
    private VideosModule relatedVideos;

    /**
     * The videoResult property.
     */
    @JsonProperty(value = "videoResult", access = JsonProperty.Access.WRITE_ONLY)
    private VideoObject videoResult;

    /**
     * Get the relatedVideos value.
     *
     * @return the relatedVideos value
     */
    public VideosModule relatedVideos() {
        return this.relatedVideos;
    }

    /**
     * Get the videoResult value.
     *
     * @return the videoResult value
     */
    public VideoObject videoResult() {
        return this.videoResult;
    }

}
