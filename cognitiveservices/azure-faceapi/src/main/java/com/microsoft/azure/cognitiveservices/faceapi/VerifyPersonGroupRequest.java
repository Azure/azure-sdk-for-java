/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for verify operation.
 */
public class VerifyPersonGroupRequest {
    /**
     * faceId the face, comes from Face - Detect.
     */
    @JsonProperty(value = "faceId", required = true)
    private UUID faceId;

    /**
     * Specify a certain person in a person group. personId is created in
     * Persons.Create.
     */
    @JsonProperty(value = "personId", required = true)
    private UUID personId;

    /**
     * Using existing personGroupId and personId for fast loading a specified
     * person. personGroupId is created in Person Groups.Create.
     */
    @JsonProperty(value = "personGroupId", required = true)
    private String personGroupId;

    /**
     * Get the faceId value.
     *
     * @return the faceId value
     */
    public UUID faceId() {
        return this.faceId;
    }

    /**
     * Set the faceId value.
     *
     * @param faceId the faceId value to set
     * @return the VerifyPersonGroupRequest object itself.
     */
    public VerifyPersonGroupRequest withFaceId(UUID faceId) {
        this.faceId = faceId;
        return this;
    }

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
     * @return the VerifyPersonGroupRequest object itself.
     */
    public VerifyPersonGroupRequest withPersonId(UUID personId) {
        this.personId = personId;
        return this;
    }

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
     * @return the VerifyPersonGroupRequest object itself.
     */
    public VerifyPersonGroupRequest withPersonGroupId(String personGroupId) {
        this.personGroupId = personGroupId;
        return this;
    }

}
