package com.microsoft.windowsazure.services.media.implementation.templates.widevine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WidevineMessage {
    /**
     * Controls which content keys should be included in a license. Only one of
     * allowed_track_types and content_key_specs can be specified.
     */
    @JsonProperty("allowed_track_types")
    private AllowedTrackTypes allowedTrackTypes;

    /**
     * A finer grained control on what content keys to return. Only one of
     * allowed_track_types and content_key_specs can be specified.
     */
    @JsonProperty("content_key_specs")
    private ContentKeySpecs[] contentKeySpecs;

    /**
     * Policy settings for this license. In the event this asset has a
     * pre-defined policy, these specified values will be used.
     */
    @JsonProperty("policy_overrides")
    private Object policyOverrides;

    @JsonProperty("allowed_track_types")
    public AllowedTrackTypes getAllowedTrackTypes() {
        return allowedTrackTypes;
    }

    @JsonProperty("allowed_track_types")
    public void setAllowedTrackTypes(AllowedTrackTypes allowedTrackTypes) {
        this.allowedTrackTypes = allowedTrackTypes;
    }

    @JsonProperty("content_key_specs")
    public ContentKeySpecs[] getContentKeySpecs() {
        return contentKeySpecs;
    }

    @JsonProperty("content_key_specs")
    public void setContentKeySpecs(ContentKeySpecs[] contentKeySpecs) {
        this.contentKeySpecs = contentKeySpecs;
    }

    @JsonProperty("policy_overrides")
    public Object getPolicyOverrides() {
        return policyOverrides;
    }

    @JsonProperty("policy_overrides")
    public void setPolicyOverrides(Object policyOverrides) {
        this.policyOverrides = policyOverrides;
    }
}
