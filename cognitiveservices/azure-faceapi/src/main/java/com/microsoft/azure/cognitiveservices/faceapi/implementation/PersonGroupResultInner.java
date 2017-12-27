/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Person group object.
 */
public class PersonGroupResultInner {
    /**
     * personGroupId of the existing person groups.
     */
    @JsonProperty(value = "personGroupId", required = true)
    private String personGroupId;

    /**
     * Person group's display name, maximum length is 128.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * User-provided data attached to this person group. Length should not
     * exceed 16KB.
     */
    @JsonProperty(value = "userData")
    private String userData;

    /**
     * Get the personGroupId value.
     *
     * @return the personGroupId value
     */
    public String personGroupId() {
        return this.personGroupId;
    }

    /**
     * Set the personGroupId value.
     *
     * @param personGroupId the personGroupId value to set
     * @return the PersonGroupResultInner object itself.
     */
    public PersonGroupResultInner withPersonGroupId(String personGroupId) {
        this.personGroupId = personGroupId;
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
     * @return the PersonGroupResultInner object itself.
     */
    public PersonGroupResultInner withName(String name) {
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
     * @return the PersonGroupResultInner object itself.
     */
    public PersonGroupResultInner withUserData(String userData) {
        this.userData = userData;
        return this;
    }

}
