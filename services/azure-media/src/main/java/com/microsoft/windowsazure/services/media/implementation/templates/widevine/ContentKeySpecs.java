package com.microsoft.windowsazure.services.media.implementation.templates.widevine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentKeySpecs {	
	/**
	 * A track type name.
	 */
	@JsonProperty
	public String track_type;	
	
	/**
	 * Unique identifier for the key.
	 */
	@JsonProperty
	public String key_id;
	
	/**
	 * Defines client robustness requirements for playback.
     *  1 - Software-based whitebox crypto is required.
     *  2 - Software crypto and an obfuscated decoder is required.
     *  3 - The key material and crypto operations must be performed 
     *      within a hardware backed trusted execution environment.
     *  4 - The crypto and decoding of content must be performed within 
     *      a hardware backed trusted execution environment.
     *  5 - The crypto, decoding and all handling of the media (compressed 
     *      and uncompressed) must be handled within a hardware backed trusted 
     *      execution environment.
	 */
	@JsonProperty
	public Integer security_level;
	 
	/**
	 * Indicates whether HDCP V1 or V2 is required or not.
	 */
	@JsonProperty
	public RequiredOutputProtection required_output_protection;
}
