// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Builder for creating instances of {@link VoiceLiveAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = { VoiceLiveAsyncClient.class })
public final class VoiceLiveClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveClientBuilder.class);
    private static final String DEFAULT_API_VERSION = "2025-10-01";

    private URI endpoint;
    private AzureKeyCredential keyCredential;
    private TokenCredential tokenCredential;
    private String apiVersion;
    private final HttpHeaders additionalHeaders = new HttpHeaders();

    /**
     * Creates a new instance of VoiceLiveClientBuilder.
     */
    public VoiceLiveClientBuilder() {
        this.apiVersion = DEFAULT_API_VERSION;
    }

    /**
     * Sets the service endpoint.
     *
     * @param endpoint The service endpoint URL.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code endpoint} is null.
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed as a URI.
     */
    public VoiceLiveClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        try {
            URI parsed = new URI(endpoint);
            this.endpoint = parsed;
        } catch (URISyntaxException ex) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(String.format("Invalid endpoint URI: '%s'", endpoint), ex));
        }
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used for authentication.
     *
     * @param keyCredential The API key credential.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code keyCredential} is null.
     */
    public VoiceLiveClientBuilder credential(AzureKeyCredential keyCredential) {
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used for authentication.
     *
     * @param tokenCredential The token credential.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code tokenCredential} is null.
     */
    public VoiceLiveClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null");
        return this;
    }

    /**
     * Sets the API version.
     *
     * @param apiVersion The API version.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Adds a header to be included in all requests.
     *
     * @param name The header name.
     * @param value The header value.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder addHeader(HttpHeaderName name, String value) {
        Objects.requireNonNull(name, "'name' cannot be null");
        Objects.requireNonNull(value, "'value' cannot be null");
        this.additionalHeaders.set(name, value);
        return this;
    }

    /**
     * Builds a {@link VoiceLiveAsyncClient} instance with the configured options.
     *
     * @return A new VoiceLiveAsyncClient instance.
     * @throws NullPointerException if endpoint is not set.
     * @throws IllegalStateException if neither keyCredential nor tokenCredential is set.
     */
    public VoiceLiveAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        if (keyCredential == null && tokenCredential == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Either 'keyCredential' or 'tokenCredential' must be set"));
        }

        String apiVersion = this.apiVersion != null ? this.apiVersion : DEFAULT_API_VERSION;

        if (keyCredential != null) {
            return new VoiceLiveAsyncClient(endpoint, keyCredential, apiVersion, additionalHeaders);
        } else {
            return new VoiceLiveAsyncClient(endpoint, tokenCredential, apiVersion, additionalHeaders);
        }
    }
}
