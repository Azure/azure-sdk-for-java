/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Persisted face result.
 */
public class PersistedFaceResultInner {
    /**
     * persistedFaceId of candidate face when find by faceListId.
     * persistedFaceId in face list is persisted and will not expire. As showed
     * in below response.
     */
    @JsonProperty(value = "persistedFaceId", required = true)
    private UUID persistedFaceId;

    /**
     * Get the persistedFaceId value.
     *
     * @return the persistedFaceId value
     */
    public UUID persistedFaceId() {
        return this.persistedFaceId;
    }

    /**
     * Set the persistedFaceId value.
     *
     * @param persistedFaceId the persistedFaceId value to set
     * @return the PersistedFaceResultInner object itself.
     */
    public PersistedFaceResultInner withPersistedFaceId(UUID persistedFaceId) {
        this.persistedFaceId = persistedFaceId;
        return this;
    }

}
