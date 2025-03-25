// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import com.azure.v2.identity.implementation.models.HttpPipelineOptions;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.microsoft.aad.msal4j.IHttpClient;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.HttpRequest;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;

import java.io.IOException;
import java.net.URI;
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
    private static final String ACCOUNT_IDENTIFIER_LOG_MESSAGE
        = "[Authenticated account] Client ID: {0}, Tenant ID: {1}"
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
    private final HttpPipelineOptions httpPipelineOptions;

    HttpPipelineAdapter(HttpPipeline httpPipeline, HttpPipelineOptions httpPipelineOptions) {
        this.httpPipeline = httpPipeline;
        this.httpPipelineOptions = httpPipelineOptions;
    }

    @Override
    public IHttpResponse send(HttpRequest httpRequest) {
        // convert request
        io.clientcore.core.http.models.HttpRequest request = new io.clientcore.core.http.models.HttpRequest();

        if (httpRequest.headers() != null) {
            HttpHeaders httpHeaders = new HttpHeaders(httpRequest.headers().size());
            httpRequest.headers().forEach((k, v) -> httpHeaders.add(HttpHeaderName.fromString(k), v));
            request.setHeaders(httpHeaders);
        }

        request.setMethod(HttpMethod.valueOf(httpRequest.httpMethod().name()));
        request.setUri(URI.create(httpRequest.url().toString()));

        if (httpRequest.body() != null) {
            request.setBody(BinaryData.fromString(httpRequest.body()));
        }

        Response<BinaryData> response = httpPipeline.send(request);
        String body = response.getValue().toString();

        logAccountIdentifiersIfConfigured(body);
        com.microsoft.aad.msal4j.HttpResponse httpResponse
            = new com.microsoft.aad.msal4j.HttpResponse().statusCode(response.getStatusCode());
        if (!CoreUtils.isNullOrEmpty(body)) {
            httpResponse.body(body);
        }
        httpResponse.addHeaders(response.getHeaders()
            .stream()
            .collect(Collectors.toMap(header -> header.getName().toString(), HttpHeader::getValues)));
        return httpResponse;
    }

    private void logAccountIdentifiersIfConfigured(String body) {
        try {
            String accessToken = IdentityUtil.getAccessToken(body);
            if (accessToken != null) {
                String[] base64Metadata = accessToken.split("\\.");
                if (base64Metadata.length > 1) {
                    byte[] decoded = Base64.getDecoder().decode(base64Metadata[1]);
                    String data = new String(decoded, StandardCharsets.UTF_8);

                    Map<String, String> jsonMap = IdentityUtil.parseJsonIntoMap(data);

                    String appId
                        = jsonMap.containsKey(APPLICATION_ID_JSON_KEY) ? jsonMap.get(APPLICATION_ID_JSON_KEY) : null;
                    String objectId = jsonMap.containsKey(OBJECT_ID_JSON_KEY) ? jsonMap.get(OBJECT_ID_JSON_KEY) : null;
                    String tenantId = jsonMap.containsKey(TENANT_ID_JSON_KEY) ? jsonMap.get(TENANT_ID_JSON_KEY) : null;
                    String userPrincipalName = jsonMap.containsKey(USER_PRINCIPAL_NAME_JSON_KEY)
                        ? jsonMap.get(USER_PRINCIPAL_NAME_JSON_KEY)
                        : null;

                    CLIENT_LOGGER.atLevel(LogLevel.INFORMATIONAL)
                        .log(MessageFormat.format(ACCOUNT_IDENTIFIER_LOG_MESSAGE,
                            getAccountIdentifierMessage(APPLICATION_IDENTIFIER, appId),
                            getAccountIdentifierMessage(TENANT_ID, tenantId),
                            getAccountIdentifierMessage(USER_PRINCIPAL_NAME, userPrincipalName),
                            getAccountIdentifierMessage(OBJECT_ID, objectId)));
                }
            }
        } catch (IOException e) {
            CLIENT_LOGGER.logThrowableAsWarning(e);
        }
    }

    private String getAccountIdentifierMessage(String identifierName, String identifierValue) {
        if (identifierValue == null) {
            return "No " + identifierName + " available.";
        }
        return identifierValue;
    }
}
