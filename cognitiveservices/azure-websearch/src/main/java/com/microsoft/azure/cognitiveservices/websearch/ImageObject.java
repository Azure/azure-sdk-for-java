/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines an image.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = ImageObject.class)
@JsonTypeName("ImageObject")
public class ImageObject extends MediaObject {
    /**
     * The URL to a thumbnail of the image.
     */
    @JsonProperty(value = "thumbnail", access = JsonProperty.Access.WRITE_ONLY)
    private ImageObject thumbnail;

    /**
     * Get the thumbnail value.
     *
     * @return the thumbnail value
     */
    public ImageObject thumbnail() {
        return this.thumbnail;
    }

}
