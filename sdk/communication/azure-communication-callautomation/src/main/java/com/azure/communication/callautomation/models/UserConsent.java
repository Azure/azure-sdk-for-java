// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The UserConsent model. */
@Fluent
public final class UserConsent {
    /*
     * The recording property.
     */
    @JsonProperty(value = "recording")
    private Integer recording;

    /** Creates an instance of UserConsent class. */
    public UserConsent() {}

    /**
     * Get the recording property: The recording property.
     *
     * @return the recording value.
     */
    public Integer getRecording() {
        return this.recording;
    }

    /**
     * Set the recording property: The recording property.
     *
     * @param recording the recording value to set.
     * @return the UserConsent object itself.
     */
    public UserConsent setRecording(Integer recording) {
        this.recording = recording;
        return this;
    }
}
