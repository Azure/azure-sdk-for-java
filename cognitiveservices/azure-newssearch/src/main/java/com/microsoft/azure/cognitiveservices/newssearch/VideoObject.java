/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a video object that is relevant to the query.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
    defaultImpl = VideoObject.class)
@JsonTypeName("VideoObject")
public class VideoObject extends MediaObject {
    /**
     * The motionThumbnailUrl property.
     */
    @JsonProperty(value = "motionThumbnailUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String motionThumbnailUrl;

    /**
     * The motionThumbnailId property.
     */
    @JsonProperty(value = "motionThumbnailId", access = JsonProperty.Access.WRITE_ONLY)
    private String motionThumbnailId;

    /**
     * The embedHtml property.
     */
    @JsonProperty(value = "embedHtml", access = JsonProperty.Access.WRITE_ONLY)
    private String embedHtml;

    /**
     * The allowHttpsEmbed property.
     */
    @JsonProperty(value = "allowHttpsEmbed", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean allowHttpsEmbed;

    /**
     * The viewCount property.
     */
    @JsonProperty(value = "viewCount", access = JsonProperty.Access.WRITE_ONLY)
    private Integer viewCount;

    /**
     * The thumbnail property.
     */
    @JsonProperty(value = "thumbnail", access = JsonProperty.Access.WRITE_ONLY)
    private ImageObject thumbnail;

    /**
     * The videoId property.
     */
    @JsonProperty(value = "videoId", access = JsonProperty.Access.WRITE_ONLY)
    private String videoId;

    /**
     * The allowMobileEmbed property.
     */
    @JsonProperty(value = "allowMobileEmbed", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean allowMobileEmbed;

    /**
     * The isSuperfresh property.
     */
    @JsonProperty(value = "isSuperfresh", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isSuperfresh;

    /**
     * Get the motionThumbnailUrl value.
     *
     * @return the motionThumbnailUrl value
     */
    public String motionThumbnailUrl() {
        return this.motionThumbnailUrl;
    }

    /**
     * Get the motionThumbnailId value.
     *
     * @return the motionThumbnailId value
     */
    public String motionThumbnailId() {
        return this.motionThumbnailId;
    }

    /**
     * Get the embedHtml value.
     *
     * @return the embedHtml value
     */
    public String embedHtml() {
        return this.embedHtml;
    }

    /**
     * Get the allowHttpsEmbed value.
     *
     * @return the allowHttpsEmbed value
     */
    public Boolean allowHttpsEmbed() {
        return this.allowHttpsEmbed;
    }

    /**
     * Get the viewCount value.
     *
     * @return the viewCount value
     */
    public Integer viewCount() {
        return this.viewCount;
    }

    /**
     * Get the thumbnail value.
     *
     * @return the thumbnail value
     */
    public ImageObject thumbnail() {
        return this.thumbnail;
    }

    /**
     * Get the videoId value.
     *
     * @return the videoId value
     */
    public String videoId() {
        return this.videoId;
    }

    /**
     * Get the allowMobileEmbed value.
     *
     * @return the allowMobileEmbed value
     */
    public Boolean allowMobileEmbed() {
        return this.allowMobileEmbed;
    }

    /**
     * Get the isSuperfresh value.
     *
     * @return the isSuperfresh value
     */
    public Boolean isSuperfresh() {
        return this.isSuperfresh;
    }

}
