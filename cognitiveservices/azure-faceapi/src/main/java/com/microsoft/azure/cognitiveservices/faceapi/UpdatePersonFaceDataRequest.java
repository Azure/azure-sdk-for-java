/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to update person face data.
 */
public class UpdatePersonFaceDataRequest {
    /**
     * User-provided data attached to the face. The size limit is 1KB.
     */
    @JsonProperty(value = "userData")
    private String userData;

    /**
     * Get the userData value.
     *
     * @return the userData value
     */
    public String userData() {
        return this.userData;
    }

    /**
     * Set the userData value.
     *
     * @param userData the userData value to set
     * @return the UpdatePersonFaceDataRequest object itself.
     */
    public UpdatePersonFaceDataRequest withUserData(String userData) {
        this.userData = userData;
        return this;
    }

}
