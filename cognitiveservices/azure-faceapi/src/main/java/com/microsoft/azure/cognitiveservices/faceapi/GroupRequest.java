/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for group request.
 */
public class GroupRequest {
    /**
     * Array of candidate faceId created by Face - Detect. The maximum is 1000
     * faces.
     */
    @JsonProperty(value = "faceIds", required = true)
    private List<String> faceIds;

    /**
     * Get the faceIds value.
     *
     * @return the faceIds value
     */
    public List<String> faceIds() {
        return this.faceIds;
    }

    /**
     * Set the faceIds value.
     *
     * @param faceIds the faceIds value to set
     * @return the GroupRequest object itself.
     */
    public GroupRequest withFaceIds(List<String> faceIds) {
        this.faceIds = faceIds;
        return this;
    }

}
