package com.microsoft.windowsazure.services.media.implementation.templates.widevine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequiredOutputProtection {
	/**
	 * Indicates whether HDCP is required.
	 */
	@JsonProperty
	public Hdcp hdcp;
}
