package com.microsoft.windowsazure.services.media.implementation.templates.widevine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WidevineMessage {
	/**
	 * Controls which content keys should be included in a license. 
     * Only one of allowed_track_types and content_key_specs can be specified.
     */
	@JsonProperty
    public AllowedTrackTypes allowed_track_types;

    /**
     * A finer grained control on what content keys to return. 
     * Only one of allowed_track_types and content_key_specs can be specified.
     */
	@JsonProperty
    public ContentKeySpecs[] content_key_specs;

	/**
     * Policy settings for this license. In the event this asset has 
     * a pre-defined policy, these specified values will be used.
     */
	@JsonProperty
    public Object policy_overrides;
}
