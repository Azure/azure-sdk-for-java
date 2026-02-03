// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.data.appconfiguration.implementation;

import com.azure.v2.data.appconfiguration.ConfigurationClientBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;
import io.clientcore.core.models.binarydata.BinaryData;

import java.util.Objects;

/**
 * A policy that authenticates requests with Azure App Configuration service. The content added by this policy
 * is leveraged in {@link ConfigurationClientCredentials} to generate the correct "Authorization" header value.
 *
 * @see ConfigurationClientCredentials
 * @see ConfigurationClientBuilder
 */
public final class ConfigurationCredentialsPolicy implements HttpPipelinePolicy {
    // "Host", "Date", and "x-ms-content-sha256" are required to generate "Authorization" value in
    // ConfigurationClientCredentials.

    private final ConfigurationClientCredentials credentials;

    /**
     * Creates an instance that is able to apply a {@link ConfigurationClientCredentials} credential to a request in the
     * pipeline.
     *
     * @param credentials the credential information to authenticate to Azure App Configuration service
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ConfigurationCredentialsPolicy(ConfigurationClientCredentials credentials) {
        Objects.requireNonNull(credentials, "'credential' can not be a null value.");
        this.credentials = credentials;
    }

    /**
     * Adds the required headers to authenticate a request to Azure App Configuration service.
     *
     * @param httpRequest The request context
     * @param httpPipelineNextPolicy The next HTTP pipeline policy to process the {@code context's} request after
     *                               this policy completes.
     * @return A {@link Response} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy httpPipelineNextPolicy) {
        credentials.setAuthorizationHeaders(httpRequest);

        return httpPipelineNextPolicy.process();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePolicy.super.getPipelinePosition();
    }
}
