/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body for identify face operation.
 */
public class IdentifyResultItem {
    /**
     * faceId of the query face.
     */
    @JsonProperty(value = "faceId", required = true)
    private String faceId;

    /**
     * The candidates property.
     */
    @JsonProperty(value = "candidates", required = true)
    private List<IdentifyResultCandidate> candidates;

    /**
     * Get the faceId value.
     *
     * @return the faceId value
     */
    public String faceId() {
        return this.faceId;
    }

    /**
     * Set the faceId value.
     *
     * @param faceId the faceId value to set
     * @return the IdentifyResultItem object itself.
     */
    public IdentifyResultItem withFaceId(String faceId) {
        this.faceId = faceId;
        return this;
    }

    /**
     * Get the candidates value.
     *
     * @return the candidates value
     */
    public List<IdentifyResultCandidate> candidates() {
        return this.candidates;
    }

    /**
     * Set the candidates value.
     *
     * @param candidates the candidates value to set
     * @return the IdentifyResultItem object itself.
     */
    public IdentifyResultItem withCandidates(List<IdentifyResultCandidate> candidates) {
        this.candidates = candidates;
        return this;
    }

}
