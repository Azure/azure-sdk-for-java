/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to create a person object.
 */
public class CreatePersonRequest {
    /**
     * Display name of the target person. The maximum length is 128.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Optional fields for user-provided data attached to a person. Size limit
     * is 16KB.
     */
    @JsonProperty(value = "userData")
    private String userData;

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
     * @return the CreatePersonRequest object itself.
     */
    public CreatePersonRequest withName(String name) {
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
     * @return the CreatePersonRequest object itself.
     */
    public CreatePersonRequest withUserData(String userData) {
        this.userData = userData;
        return this;
    }

}
