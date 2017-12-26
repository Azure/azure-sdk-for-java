/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import java.util.UUID;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Person object.
 */
public class PersonResultInner {
    /**
     * personId of the target face list.
     */
    @JsonProperty(value = "personId", required = true)
    private UUID personId;

    /**
     * persistedFaceIds of registered faces in the person. These
     * persistedFaceIds are returned from Person - Add a Person Face, and will
     * not expire.
     */
    @JsonProperty(value = "persistedFaceIds")
    private List<UUID> persistedFaceIds;

    /**
     * Person's display name, maximum length is 128.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * User-provided data attached to this person. Length should not exceed
     * 16KB.
     */
    @JsonProperty(value = "userData")
    private String userData;

    /**
     * Get the personId value.
     *
     * @return the personId value
     */
    public UUID personId() {
        return this.personId;
    }

    /**
     * Set the personId value.
     *
     * @param personId the personId value to set
     * @return the PersonResultInner object itself.
     */
    public PersonResultInner withPersonId(UUID personId) {
        this.personId = personId;
        return this;
    }

    /**
     * Get the persistedFaceIds value.
     *
     * @return the persistedFaceIds value
     */
    public List<UUID> persistedFaceIds() {
        return this.persistedFaceIds;
    }

    /**
     * Set the persistedFaceIds value.
     *
     * @param persistedFaceIds the persistedFaceIds value to set
     * @return the PersonResultInner object itself.
     */
    public PersonResultInner withPersistedFaceIds(List<UUID> persistedFaceIds) {
        this.persistedFaceIds = persistedFaceIds;
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
     * @return the PersonResultInner object itself.
     */
    public PersonResultInner withName(String name) {
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
     * @return the PersonResultInner object itself.
     */
    public PersonResultInner withUserData(String userData) {
        this.userData = userData;
        return this;
    }

}
