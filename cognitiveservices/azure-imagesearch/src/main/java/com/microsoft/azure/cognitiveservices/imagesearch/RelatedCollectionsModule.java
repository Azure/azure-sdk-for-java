/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a list of webpages that contain related images.
 */
public class RelatedCollectionsModule {
    /**
     * A list of webpages that contain related images.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private List<ImageGallery> value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<ImageGallery> value() {
        return this.value;
    }

}
