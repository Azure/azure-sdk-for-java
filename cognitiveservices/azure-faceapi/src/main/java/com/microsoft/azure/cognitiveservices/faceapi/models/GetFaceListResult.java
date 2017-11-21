/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of the GetFaceList operation.
 */
public class GetFaceListResult {
    /**
     * faceListId of the target face list.
     */
    @JsonProperty(value = "faceListId", required = true)
    private String faceListId;

    /**
     * Face list's display name.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * User-provided data attached to this face list.
     */
    @JsonProperty(value = "userData")
    private String userData;

    /**
     * Get the faceListId value.
     *
     * @return the faceListId value
     */
    public String faceListId() {
        return this.faceListId;
    }

    /**
     * Set the faceListId value.
     *
     * @param faceListId the faceListId value to set
     * @return the GetFaceListResult object itself.
     */
    public GetFaceListResult withFaceListId(String faceListId) {
        this.faceListId = faceListId;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the GetFaceListResult object itself.
     */
    public GetFaceListResult withName(String name) {
        this.name = name;
        return this;
    }

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
     * @return the GetFaceListResult object itself.
     */
    public GetFaceListResult withUserData(String userData) {
        this.userData = userData;
        return this;
    }

}
