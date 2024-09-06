// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import com.azure.identity.implementation.util.IdentityUtil;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IHttpClient;
import com.microsoft.aad.msal4j.IHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapts an HttpPipeline to an instance of IHttpClient in the MSAL4j pipeline.
 */
class HttpPipelineAdapter implements IHttpClient {
    private static final ClientLogger CLIENT_LOGGER = new ClientLogger(HttpPipelineAdapter.class);
    private static final String ACCOUNT_IDENTIFIER_LOG_MESSAGE = "[Authenticated account] Client ID: {0}, Tenant ID: {1}"
        + ", User Principal Name: {2}, Object ID (user): {3})";
    private static final String APPLICATION_IDENTIFIER = "Application Identifier";
    private static final String OBJECT_ID = "Object Id";
    private static final String TENANT_ID = "Tenant Id";
    private static final String USER_PRINCIPAL_NAME = "User Principal Name";
    private static final String APPLICATION_ID_JSON_KEY = "appid";
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

        HttpResponse response = httpPipeline.sendSync(request, Context.NONE);
        String body = response.getBodyAsBinaryData().toString();

        logAccountIdentifiersIfConfigured(body);
        com.microsoft.aad.msal4j.HttpResponse httpResponse = new com.microsoft.aad.msal4j.HttpResponse()
            .statusCode(response.getStatusCode());
        if (!CoreUtils.isNullOrEmpty(body)) {
            httpResponse.body(body);
        }
        httpResponse.addHeaders(response.getHeaders().stream().collect(Collectors.toMap(HttpHeader::getName,
            HttpHeader::getValuesList)));
        return httpResponse;
    }

    private void logAccountIdentifiersIfConfigured(String body) {
        if (identityClientOptions == null
            || !identityClientOptions.getIdentityLogOptionsImpl().isLoggingAccountIdentifiersAllowed()) {
            return;
        }
        try {
            String accessToken = IdentityUtil.getAccessToken(body);
            if (accessToken != null) {
                String[] base64Metadata = accessToken.split("\\.");
                if (base64Metadata.length > 1) {
                    byte[] decoded = Base64.getDecoder().decode(base64Metadata[1]);
                    String data = new String(decoded, StandardCharsets.UTF_8);


                    Map<String, String> jsonMap = IdentityUtil.parseJsonIntoMap(data);

                    String appId = jsonMap.containsKey(APPLICATION_ID_JSON_KEY)
                        ? jsonMap.get(APPLICATION_ID_JSON_KEY) : null;
                    String objectId = jsonMap.containsKey(OBJECT_ID_JSON_KEY)
                        ? jsonMap.get(OBJECT_ID_JSON_KEY) : null;
                    String tenantId = jsonMap.containsKey(TENANT_ID_JSON_KEY)
                        ? jsonMap.get(TENANT_ID_JSON_KEY) : null;
                    String userPrincipalName = jsonMap.containsKey(USER_PRINCIPAL_NAME_JSON_KEY)
                        ? jsonMap.get(USER_PRINCIPAL_NAME_JSON_KEY) : null;

                    CLIENT_LOGGER.log(LogLevel.INFORMATIONAL, () -> MessageFormat
                        .format(ACCOUNT_IDENTIFIER_LOG_MESSAGE,
                            getAccountIdentifierMessage(APPLICATION_IDENTIFIER, appId),
                            getAccountIdentifierMessage(TENANT_ID, tenantId),
                            getAccountIdentifierMessage(USER_PRINCIPAL_NAME, userPrincipalName),
                            getAccountIdentifierMessage(OBJECT_ID, objectId)));
                }
            }
        } catch (IOException e) {
            CLIENT_LOGGER.log(LogLevel.WARNING, () -> "allowLoggingAccountIdentifiers Log option was set,"
                + " but the account information could not be logged.", e);
        }
    }

    private String getAccountIdentifierMessage(String identifierName, String identifierValue) {
        if (identifierValue == null) {
            return "No " + identifierName + " available.";
        }
        return identifierValue;
    }
}
