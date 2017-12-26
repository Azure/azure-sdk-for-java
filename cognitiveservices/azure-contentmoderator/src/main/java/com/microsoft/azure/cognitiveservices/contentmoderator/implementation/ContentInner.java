/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The ContentInner model.
 */
public class ContentInner {
    /**
     * Content to evaluate for a job.
     */
    @JsonProperty(value = "ContentValue", required = true)
    private String contentValue;

    /**
     * Get the contentValue value.
     *
     * @return the contentValue value
     */
    public String contentValue() {
        return this.contentValue;
    }

    /**
     * Set the contentValue value.
     *
     * @param contentValue the contentValue value to set
     * @return the ContentInner object itself.
     */
    public ContentInner withContentValue(String contentValue) {
        this.contentValue = contentValue;
        return this;
    }

}
