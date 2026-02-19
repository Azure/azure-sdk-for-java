// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpHeaders;

/**
 * Options for customizing VoiceLive requests with additional query parameters and headers.
 */
@Fluent
public final class VoiceLiveRequestOptions {

    private Map<String, String> customQueryParameters;
    private HttpHeaders customHeaders;

    /**
     * Creates a new instance of VoiceLiveRequestOptions.
     */
    public VoiceLiveRequestOptions() {
        this.customQueryParameters = new HashMap<>();
        this.customHeaders = new HttpHeaders();
    }

    /**
     * Gets the custom query parameters.
     *
     * @return The custom query parameters.
     */
    public Map<String, String> getCustomQueryParameters() {
        return customQueryParameters;
    }

    /**
     * Adds a custom query parameter.
     *
     * @param key The query parameter key.
     * @param value The query parameter value.
     * @return The updated VoiceLiveRequestOptions object.
     * @throws NullPointerException if {@code key} is null.
     */
    public VoiceLiveRequestOptions addCustomQueryParameter(String key, String value) {
        Objects.requireNonNull(key, "'key' cannot be null");
        if (this.customQueryParameters == null) {
            this.customQueryParameters = new HashMap<>();
        }
        this.customQueryParameters.put(key, value);
        return this;
    }

    /**
     * Gets the custom headers.
     *
     * @return The custom headers.
     */
    public HttpHeaders getCustomHeaders() {
        return customHeaders;
    }

    /**
     * Sets the custom headers.
     *
     * @param customHeaders The custom headers to set.
     * @return The updated VoiceLiveRequestOptions object.
     */
    public VoiceLiveRequestOptions setCustomHeaders(HttpHeaders customHeaders) {
        this.customHeaders = customHeaders != null ? customHeaders : new HttpHeaders();
        return this;
    }

    /**
     * Adds a custom header.
     *
     * @param name The header name.
     * @param value The header value.
     * @return The updated VoiceLiveRequestOptions object.
     * @throws NullPointerException if {@code name} is null.
     */
    public VoiceLiveRequestOptions addCustomHeader(String name, String value) {
        Objects.requireNonNull(name, "'name' cannot be null");
        if (this.customHeaders == null) {
            this.customHeaders = new HttpHeaders();
        }
        this.customHeaders.set(name, value);
        return this;
    }
}
