/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.computervision.CelebritiesModel;
import com.microsoft.azure.cognitiveservices.computervision.ImageMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Result of image analysis using a specific domain model including additional
 * metadata.
 */
@JsonFlatten
public class DomainModelResultsInner {
    /**
     * An array of possible celebritied identified in the image.
     */
    @JsonProperty(value = "result.celebrities")
    private List<CelebritiesModel> celebrities;

    /**
     * Id of the REST API request.
     */
    @JsonProperty(value = "requestId")
    private String requestId;

    /**
     * Additional image metadata.
     */
    @JsonProperty(value = "metadata")
    private ImageMetadata metadata;

    /**
     * Get the celebrities value.
     *
     * @return the celebrities value
     */
    public List<CelebritiesModel> celebrities() {
        return this.celebrities;
    }

    /**
     * Set the celebrities value.
     *
     * @param celebrities the celebrities value to set
     * @return the DomainModelResultsInner object itself.
     */
    public DomainModelResultsInner withCelebrities(List<CelebritiesModel> celebrities) {
        this.celebrities = celebrities;
        return this;
    }

    /**
     * Get the requestId value.
     *
     * @return the requestId value
     */
    public String requestId() {
        return this.requestId;
    }

    /**
     * Set the requestId value.
     *
     * @param requestId the requestId value to set
     * @return the DomainModelResultsInner object itself.
     */
    public DomainModelResultsInner withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public ImageMetadata metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the DomainModelResultsInner object itself.
     */
    public DomainModelResultsInner withMetadata(ImageMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
