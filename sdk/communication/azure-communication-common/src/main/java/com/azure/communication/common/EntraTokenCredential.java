// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.communication.common.implementation.EntraTokenGuardPolicy;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.azure.communication.common.EntraCommunicationTokenUtils.allScopesStartWith;
import static com.azure.communication.common.EntraCommunicationTokenUtils.COMMUNICATION_CLIENTS_SCOPE_PREFIX;
import static com.azure.communication.common.EntraCommunicationTokenUtils.COMMUNICATION_CLIENTS_ENDPOINT;
import static com.azure.communication.common.EntraCommunicationTokenUtils.COMMUNICATION_CLIENTS_API_VERSION;
import static com.azure.communication.common.EntraCommunicationTokenUtils.TEAMS_EXTENSION_SCOPE_PREFIX;
import static com.azure.communication.common.EntraCommunicationTokenUtils.TEAMS_EXTENSION_ENDPOINT;
import static com.azure.communication.common.EntraCommunicationTokenUtils.TEAMS_EXTENSION_API_VERSION;

/**
 * Represents a credential that exchanges an Entra token for an Azure Communication Services (ACS) token, enabling access to ACS resources.
 */
final class EntraTokenCredential implements AutoCloseable {

    private final ClientLogger logger = new ClientLogger(EntraTokenCredential.class);
    private final String resourceEndpoint;
    private final List<String> scopes;
    private HttpPipeline pipeline;

    /**
     * Creates an EntraTokenCredential using the provided EntraCommunicationTokenCredentialOptions.
     * This constructor is intended for scenarios where an Entra user token is required for Azure Communication Services.
     *
     * @param entraTokenOptions The options for configuring the Entra token credential.
     */
    EntraTokenCredential(EntraCommunicationTokenCredentialOptions entraTokenOptions) {
        this(entraTokenOptions, null);
    }

    /**
     * For testing purposes: Creates an EntraTokenCredential using the provided EntraCommunicationTokenCredentialOptions and HttpClient.
     * This constructor is intended for scenarios where an Entra user token is required for Azure Communication Services.
     *
     * @param entraTokenOptions The options for configuring the Entra token credential.
     * @param httpClient The HTTP client to use for making requests.
     */
    EntraTokenCredential(EntraCommunicationTokenCredentialOptions entraTokenOptions, HttpClient httpClient) {
        this.resourceEndpoint = entraTokenOptions.getResourceEndpoint();
        this.scopes = new ArrayList<>(entraTokenOptions.getScopes());
        this.pipeline = createPipelineFromOptions(entraTokenOptions, httpClient);

        this.exchangeEntraToken().subscribe();
    }

    private HttpPipeline createPipelineFromOptions(EntraCommunicationTokenCredentialOptions entraTokenOptions,
        HttpClient httpClient) {
        BearerTokenAuthenticationPolicy authPolicy = new BearerTokenAuthenticationPolicy(
            entraTokenOptions.getTokenCredential(), scopes.toArray(new String[0]));
        HttpPipelinePolicy guardPolicy = new EntraTokenGuardPolicy();
        RetryPolicy retryPolicy = new RetryPolicy();
        HttpClient clientToUse = (httpClient != null) ? httpClient : HttpClient.createDefault();

        return new HttpPipelineBuilder().httpClient(clientToUse).policies(authPolicy, guardPolicy, retryPolicy).build();
    }

    /**
     * Get Access token by exchanging Entra token.
     *
     * @return Asynchronous call to fetch access token
     */
    public Mono<String> exchangeEntraToken() {
        HttpRequest request = createRequest();
        synchronized (this) {
            return pipeline.send(request).flatMap(response -> {
                if (response.getStatusCode() == 200) {
                    return parseAccessTokenFromResponse(response);
                } else {
                    return response.getBodyAsString()
                        .defaultIfEmpty("")
                        .flatMap(body -> FluxUtil.monoError(logger,
                            new HttpResponseException("Failed to exchange Entra token : " + body, response)));
                }
            });
        }
    }

    @Override
    public void close() {
        this.pipeline = null;
    }

    private HttpRequest createRequest() {
        String url = createRequestUrl();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url);
        request.setHeader(HttpHeaderName.ACCEPT, "application/json");
        request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
        request.setBody("{}".getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private String createRequestUrl() {
        String[] endpointAndVersion = determineEndpointAndApiVersion();
        String endpoint = endpointAndVersion[0];
        String apiVersion = endpointAndVersion[1];
        return resourceEndpoint + endpoint + "?api-version=" + apiVersion;
    }

    private String[] determineEndpointAndApiVersion() {
        if (allScopesStartWith(scopes, TEAMS_EXTENSION_SCOPE_PREFIX)) {
            return new String[] { TEAMS_EXTENSION_ENDPOINT, TEAMS_EXTENSION_API_VERSION };
        } else if (allScopesStartWith(scopes, COMMUNICATION_CLIENTS_SCOPE_PREFIX)) {
            return new String[] { COMMUNICATION_CLIENTS_ENDPOINT, COMMUNICATION_CLIENTS_API_VERSION };
        } else {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Scopes validation failed. Ensure all scopes start with either "
                    + TEAMS_EXTENSION_SCOPE_PREFIX + " or " + COMMUNICATION_CLIENTS_SCOPE_PREFIX));
        }
    }

    private Mono<String> parseAccessTokenFromResponse(HttpResponse response) {
        return response.getBodyAsString(StandardCharsets.UTF_8).flatMap(body -> {
            try {
                JsonNode root = new ObjectMapper().readTree(body);
                JsonNode accessTokenNode = root.get("accessToken");
                String token = accessTokenNode.get("token").asText();

                return Mono.just(token);
            } catch (RuntimeException | IOException ex) {
                return FluxUtil.monoError(logger,
                    new HttpResponseException("Failed to parse the response : " + body, response));
            }
        });
    }
}
