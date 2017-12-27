/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to create a person group.
 */
public class CreatePersonGroupRequest {
    /**
     * Person group display name. The maximum length is 128.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * User-provided data attached to the person group. The size limit is 16KB.
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
     * @return the CreatePersonGroupRequest object itself.
     */
    public CreatePersonGroupRequest withName(String name) {
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
     * @return the CreatePersonGroupRequest object itself.
     */
    public CreatePersonGroupRequest withUserData(String userData) {
        this.userData = userData;
        return this;
    }

}
