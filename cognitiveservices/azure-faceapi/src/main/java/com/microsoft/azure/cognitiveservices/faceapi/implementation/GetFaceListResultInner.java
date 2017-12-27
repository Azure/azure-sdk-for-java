/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of the GetFaceList operation.
 */
public class GetFaceListResultInner {
    /**
     * faceListId of the target face list.
     */
    @JsonProperty(value = "faceListId", required = true)
    private String faceListId;

    /**
     * Face list's display name, maximum length is 128.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * User-provided data attached to this face list. Length should not exceed
     * 16KB.
     */
    @JsonProperty(value = "userData")
    private String userData;

    /**
     * Persisted faces within the face list.
     */
    @JsonProperty(value = "persistedFaces")
    private List<PersonFaceResultInner> persistedFaces;

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
     * @return the GetFaceListResultInner object itself.
     */
    public GetFaceListResultInner withFaceListId(String faceListId) {
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
     * @return the GetFaceListResultInner object itself.
     */
    public GetFaceListResultInner withName(String name) {
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
     * @return the GetFaceListResultInner object itself.
     */
    public GetFaceListResultInner withUserData(String userData) {
        this.userData = userData;
        return this;
    }

    /**
     * Get the persistedFaces value.
     *
     * @return the persistedFaces value
     */
    public List<PersonFaceResultInner> persistedFaces() {
        return this.persistedFaces;
    }

    /**
     * Set the persistedFaces value.
     *
     * @param persistedFaces the persistedFaces value to set
     * @return the GetFaceListResultInner object itself.
     */
    public GetFaceListResultInner withPersistedFaces(List<PersonFaceResultInner> persistedFaces) {
        this.persistedFaces = persistedFaces;
        return this;
    }

}
