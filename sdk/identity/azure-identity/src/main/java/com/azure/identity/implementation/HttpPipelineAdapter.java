// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IHttpClient;
import com.microsoft.aad.msal4j.IHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * Adapts an HttpPipeline to an instance of IHttpClient in the MSAL4j pipeline.
 */
class HttpPipelineAdapter implements IHttpClient {
    private static final ClientLogger CLIENT_LOGGER = new ClientLogger(HttpPipelineAdapter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ACCOUNT_IDENTIFIER_LOG_MESSAGE = "[Authenticated account] Client ID: {0}, Tenant ID: {1}"
        + ", User Principal Name: {2}, Object ID (user): {3})";
    private static final String APPLICATION_IDENTIFIER = "Application Identifier";
    private static final String OBJECT_ID = "Object Id";
    private static final String TENANT_ID = "Tenant Id";
    private static final String USER_PRINCIPAL_NAME = "User Principal Name";
    private static final String ACCESS_TOKEN_JSON_KEY = "access_token";
    private static final String APPLICATON_ID_JSON_KEY = "appid";
    private static final String OBJECT_ID_JSON_KEY = "oid";
    private static final String TENANT_ID_JSON_KEY = "tid";
    private static final String USER_PRINCIPAL_NAME_JSON_KEY = "upn";
    private final HttpPipeline httpPipeline;
    private IdentityClientOptions identityClientOptions;

    HttpPipelineAdapter(HttpPipeline httpPipeline, IdentityClientOptions identityClientOptions) {
        this.httpPipeline = httpPipeline;
        this.identityClientOptions = identityClientOptions;
    }

    @Override
    public IHttpResponse send(HttpRequest httpRequest) {
        // convert request
        com.azure.core.http.HttpRequest request = new com.azure.core.http.HttpRequest(
            HttpMethod.valueOf(httpRequest.httpMethod().name()),
            httpRequest.url());
        if (httpRequest.headers() != null) {
            request.setHeaders(new HttpHeaders(httpRequest.headers()));
        }
        if (httpRequest.body() != null) {
            request.setBody(httpRequest.body());
        }

        return httpPipeline.send(request)
            .flatMap(response -> response.getBodyAsString()
                .map(body -> {
                    logAccounIdentifiersIfConfigured(body);
                    com.microsoft.aad.msal4j.HttpResponse httpResponse = new com.microsoft.aad.msal4j.HttpResponse()
                        .body(body)
                        .statusCode(response.getStatusCode());
                    httpResponse.addHeaders(response.getHeaders().stream().collect(Collectors.toMap(HttpHeader::getName,
                        HttpHeader::getValuesList)));
                    return httpResponse;
                })
                // if no body
                .switchIfEmpty(Mono.defer(() -> {
                    com.microsoft.aad.msal4j.HttpResponse httpResponse = new com.microsoft.aad.msal4j.HttpResponse()
                        .statusCode(response.getStatusCode());
                    httpResponse.addHeaders(response.getHeaders().stream().collect(Collectors.toMap(HttpHeader::getName,
                        HttpHeader::getValuesList)));
                    return Mono.just(httpResponse);
                })))
            .block();
    }

    private void logAccounIdentifiersIfConfigured(String body) {
        if (identityClientOptions != null &&
            !identityClientOptions.getIdentityLogOptionsImpl().isLoggingAccountIdentifiersAllowed()) {
            return;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(body);
            JsonNode accessToken = node.get(ACCESS_TOKEN_JSON_KEY);
            if (accessToken != null) {
                String[] base64Metadata = accessToken.asText().split("\\.");
                if (base64Metadata.length > 1) {
                    byte[] decoded = Base64.getDecoder().decode(base64Metadata[1]);
                    String data = new String(decoded, StandardCharsets.UTF_8);
                    JsonNode dataNode = OBJECT_MAPPER.readTree(data);
                    JsonNode appId = dataNode.get(APPLICATON_ID_JSON_KEY);
                    JsonNode objectId = dataNode.get(OBJECT_ID_JSON_KEY);
                    JsonNode tenantId = dataNode.get(TENANT_ID_JSON_KEY);
                    JsonNode userPrincipalName = dataNode.get(USER_PRINCIPAL_NAME_JSON_KEY);

                    CLIENT_LOGGER.log(LogLevel.INFORMATIONAL, () -> MessageFormat
                        .format(ACCOUNT_IDENTIFIER_LOG_MESSAGE,
                            getAccountIdentifierMessage(APPLICATION_IDENTIFIER, appId),
                            getAccountIdentifierMessage(OBJECT_ID, objectId),
                            getAccountIdentifierMessage(TENANT_ID, tenantId),
                            getAccountIdentifierMessage(USER_PRINCIPAL_NAME,
                                userPrincipalName)));
                }
            }
        } catch (JsonProcessingException e) {
            CLIENT_LOGGER.log(LogLevel.WARNING, () -> "allowLoggingAccountIdentifiers Log option was set,"
                    + " but the account information could not be logged.", e);
        }
    }

    private String getAccountIdentifierMessage(String identifierName, JsonNode identifierValue) {
        if (identifierValue == null) {
            return "No" + identifierName + " available.";
        }
        return identifierValue.asText();
    }
}
