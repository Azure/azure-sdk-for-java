// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.KeyCredentialTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

/**
 * Builder for creating instances of {@link VoiceLiveAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = { VoiceLiveAsyncClient.class })
public final class VoiceLiveClientBuilder implements TokenCredentialTrait<VoiceLiveClientBuilder>,
    KeyCredentialTrait<VoiceLiveClientBuilder>, EndpointTrait<VoiceLiveClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveClientBuilder.class);

    private URI endpoint;
    private KeyCredential keyCredential;
    private TokenCredential tokenCredential;
    private VoiceLiveServiceVersion serviceVersion;
    private ClientOptions clientOptions;
    private OpenTelemetry openTelemetry;
    private Boolean enableContentRecording;

    /**
     * Creates a new instance of VoiceLiveClientBuilder.
     */
    public VoiceLiveClientBuilder() {
    }

    /**
     * Sets the service endpoint.
     *
     * @param endpoint The service endpoint URL.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code endpoint} is null.
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed as a URI.
     */
    @Override
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
     * Sets the {@link KeyCredential} used for authentication.
     *
     * @param keyCredential The API key credential.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code keyCredential} is null.
     */
    @Override
    public VoiceLiveClientBuilder credential(KeyCredential keyCredential) {
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
    @Override
    public VoiceLiveClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null");
        return this;
    }

    /**
     * Sets the service version.
     *
     * @param serviceVersion The service version.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder serviceVersion(VoiceLiveServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The client options.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the {@link OpenTelemetry} instance to use for tracing.
     *
     * <p>If not set, defaults to {@link GlobalOpenTelemetry#getOrNoop()}, which automatically
     * uses the OpenTelemetry instance installed by the Java agent (if present), or a no-op
     * implementation that has zero performance impact.</p>
     *
     * <p>When an OpenTelemetry SDK is configured (either globally or via this method), the SDK
     * automatically emits spans for connect, send, recv, and close operations with voice-specific
     * attributes and session-level counters following GenAI Semantic Conventions.</p>
     *
     * @param openTelemetry The OpenTelemetry instance.
     * @return The updated VoiceLiveClientBuilder instance.
     * @throws NullPointerException if {@code openTelemetry} is null.
     */
    public VoiceLiveClientBuilder openTelemetry(OpenTelemetry openTelemetry) {
        this.openTelemetry = Objects.requireNonNull(openTelemetry, "'openTelemetry' cannot be null");
        return this;
    }

    /**
     * Enables or disables content recording in trace spans.
     *
     * <p>When enabled, full JSON payloads (including audio data) will be captured in span events.
     * This is off by default for privacy. Can also be controlled via the
     * {@code AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED} environment variable.</p>
     *
     * @param enableContentRecording true to enable content recording in spans.
     * @return The updated VoiceLiveClientBuilder instance.
     */
    public VoiceLiveClientBuilder enableContentRecording(boolean enableContentRecording) {
        this.enableContentRecording = enableContentRecording;
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

        VoiceLiveServiceVersion version = serviceVersion != null ? serviceVersion : VoiceLiveServiceVersion.getLatest();
        HttpHeaders additionalHeaders = CoreUtils.createHttpHeadersFromClientOptions(clientOptions);

        OpenTelemetry otel = openTelemetry != null ? openTelemetry : GlobalOpenTelemetry.getOrNoop();
        Tracer tracer = otel.getTracer("azure-ai-voicelive", "1.0.0-beta.6");

        if (keyCredential != null) {
            return new VoiceLiveAsyncClient(endpoint, keyCredential, version.getVersion(), additionalHeaders, tracer,
                enableContentRecording);
        } else {
            return new VoiceLiveAsyncClient(endpoint, tokenCredential, version.getVersion(), additionalHeaders, tracer,
                enableContentRecording);
        }
    }
}
