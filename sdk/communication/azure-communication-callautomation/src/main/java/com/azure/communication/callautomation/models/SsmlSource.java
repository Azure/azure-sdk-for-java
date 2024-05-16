// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The SsmlSource model. */
@Fluent
public final class SsmlSource extends PlaySource {
    /*
     * Ssml string for the cognitive service to be played
     */
    @JsonProperty(value = "ssmlText", required = true)
    private String ssmlText;

    /*
     * Endpoint where the Custom Voice was deployed.
     */
    @JsonProperty(value = "customVoiceEndpointId")
    private String customVoiceEndpointId;

    /**
     * Get the ssmlText property: Ssml string for the cognitive service to be played.
     *
     * @return the ssmlText value.
     */
    public String getSsmlText() {
        return this.ssmlText;
    }

    /**
     * Set the ssmlText property: Ssml string for the cognitive service to be played.
     *
     * @param ssmlText the ssmlText value to set.
     * @return the SsmlSourceInternal object itself.
     */
    public SsmlSource setSsmlText(String ssmlText) {
        this.ssmlText = ssmlText;
        return this;
    }

    /**
     * Get the customVoiceEndpointId property: Endpoint where the custom voice was deployed.
     *
     * @return the customVoiceEndpointId value.
     */
    public String getCustomVoiceEndpointId() {
        return this.customVoiceEndpointId;
    }

    /**
     * Set the customVoiceEndpointId property: Endpoint where the custom voice was deployed.
     *
     * @param customVoiceEndpointId the customVoiceEndpointId value to set.
     * @return the TextSourceInternal object itself.
     */
    public SsmlSource setCustomVoiceEndpointId(String customVoiceEndpointId) {
        this.customVoiceEndpointId = customVoiceEndpointId;
        return this;
    }
}
