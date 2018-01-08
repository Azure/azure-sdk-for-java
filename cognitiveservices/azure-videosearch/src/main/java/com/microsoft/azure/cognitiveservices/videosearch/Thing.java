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

/**
 * The Thing model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Thing.class)
@JsonTypeName("Thing")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "CreativeWork", value = CreativeWork.class)
})
public class Thing extends Response {
    /**
     * The name of the thing represented by this object.
     */
    @JsonProperty(value = "name", access = JsonProperty.Access.WRITE_ONLY)
    private String name;

    /**
     * The URL to get more information about the thing represented by this
     * object.
     */
    @JsonProperty(value = "url", access = JsonProperty.Access.WRITE_ONLY)
    private String url;

    /**
     * The image property.
     */
    @JsonProperty(value = "image", access = JsonProperty.Access.WRITE_ONLY)
    private ImageObject image;

    /**
     * A short description of the item.
     */
    @JsonProperty(value = "description", access = JsonProperty.Access.WRITE_ONLY)
    private String description;

    /**
     * The alternateName property.
     */
    @JsonProperty(value = "alternateName", access = JsonProperty.Access.WRITE_ONLY)
    private String alternateName;

    /**
     * An ID that uniquely identifies this item.
     */
    @JsonProperty(value = "bingId", access = JsonProperty.Access.WRITE_ONLY)
    private String bingId;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Get the image value.
     *
     * @return the image value
     */
    public ImageObject image() {
        return this.image;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Get the alternateName value.
     *
     * @return the alternateName value
     */
    public String alternateName() {
        return this.alternateName;
    }

    /**
     * Get the bingId value.
     *
     * @return the bingId value
     */
    public String bingId() {
        return this.bingId;
    }

}
