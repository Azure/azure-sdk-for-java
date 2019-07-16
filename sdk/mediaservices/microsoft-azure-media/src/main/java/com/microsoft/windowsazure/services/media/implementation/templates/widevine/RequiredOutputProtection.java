package com.microsoft.windowsazure.services.media.implementation.templates.widevine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequiredOutputProtection {
    /**
     * Indicates whether HDCP is required.
     */
    @JsonProperty("hdcp")
    private Hdcp hdcp;

    @JsonProperty("hdcp")
    public Hdcp getHdcp() {
        return hdcp;
    }

    @JsonProperty("hdcp")
    public void setHdcp(Hdcp hdcp) {
        this.hdcp = hdcp;
    }
}
