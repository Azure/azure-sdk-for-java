package com.microsoft.windowsazure.services.media.implementation.templates.widevine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentKeySpecs {
    /**
     * A track type name.
     */
    @JsonProperty("track_type")
    private String trackType;

    /**
     * Unique identifier for the key.
     */
    @JsonProperty("key_id")
    private String keyId;

    /**
     * Defines client robustness requirements for playback. 1 - Software-based
     * whitebox crypto is required. 2 - Software crypto and an obfuscated
     * decoder is required. 3 - The key material and crypto operations must be
     * performed within a hardware backed trusted execution environment. 4 - The
     * crypto and decoding of content must be performed within a hardware backed
     * trusted execution environment. 5 - The crypto, decoding and all handling
     * of the media (compressed and uncompressed) must be handled within a
     * hardware backed trusted execution environment.
     */
    @JsonProperty("security_level")
    private Integer securityLevel;

    /**
     * Indicates whether HDCP V1 or V2 is required or not.
     */
    @JsonProperty("required_output_protection")
    private RequiredOutputProtection requiredOutputProtection;

    @JsonProperty("track_type")
    public String getTrackType() {
        return trackType;
    }

    @JsonProperty("track_type")
    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }

    @JsonProperty("key_id")
    public String getKeyId() {
        return keyId;
    }

    @JsonProperty("key_id")
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    @JsonProperty("security_level")
    public Integer getSecurityLevel() {
        return securityLevel;
    }

    @JsonProperty("security_level")
    public void setSecurityLevel(Integer securityLevel) {
        this.securityLevel = securityLevel;
    }

    @JsonProperty("required_output_protection")
    public RequiredOutputProtection getRequiredOutputProtection() {
        return requiredOutputProtection;
    }

    @JsonProperty("required_output_protection")
    public void setRequiredOutputProtection(RequiredOutputProtection requiredOutputProtection) {
        this.requiredOutputProtection = requiredOutputProtection;
    }
}
