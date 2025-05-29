// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.client.ConfidentialClient;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.models.OidcTokenResponse;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.JsonReader;

import java.io.IOException;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

/**
 * The {@link AzurePipelinesCredential} acquires a token using the Azure Pipelines service connection.
 * <p>
 * To construct an instance of this credential, use the {@link AzurePipelinesCredentialBuilder}:
 * <pre>
 * &#47;&#47; serviceConnectionId is retrieved from the portal.
 * &#47;&#47; systemAccessToken is retrieved from the pipeline environment as shown.
 * &#47;&#47; You may choose another name for this variable.
 *
 * String systemAccessToken = System.getenv&#40;&quot;SYSTEM_ACCESSTOKEN&quot;&#41;;
 * AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder&#40;&#41;.clientId&#40;clientId&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .serviceConnectionId&#40;serviceConnectionId&#41;
 *     .systemAccessToken&#40;systemAccessToken&#41;
 *     .build&#40;&#41;;
 * </pre>
 */
public class AzurePipelinesCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzurePipelinesCredential.class);
    private final ConfidentialClient confidentialClient;

    /**
     * Creates an instance of {@link AzurePipelinesCredential}.
     *
     * @param requestUrl the request url to get the client assertion token
     * @param systemAccessToken the system access token
     * @param confidentialClientOptions the options for configuring the confidential client
     */
    AzurePipelinesCredential(String requestUrl, String systemAccessToken,
        ConfidentialClientOptions confidentialClientOptions) {

        confidentialClientOptions.setClientAssertionFunction((httpPipeline) -> {
            try {
                HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST).setUri(requestUrl);
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add(HttpHeaderName.AUTHORIZATION, "Bearer " + systemAccessToken);
                httpHeaders.add(HttpHeaderName.CONTENT_TYPE, "application/json");
                // Prevents the service from responding with a redirect HTTP status code (useful for automation).
                httpHeaders.add(IdentityUtil.X_TFS_FED_AUTH_REDIRECT, "Suppress");
                try (Response<BinaryData> response = httpPipeline.send(request)) {
                    String responseBody = response.getValue().toString();
                    if (response.getStatusCode() != 200) {
                        String xVssHeader = response.getHeaders().getValue(IdentityUtil.X_VSS_E2EID);
                        String xMsEdgeRefHeader = response.getHeaders().getValue(IdentityUtil.X_MSEDGE_REF);
                        String message = "Failed to get the client assertion token " + responseBody + ".";
                        if (xVssHeader != null) {
                            message += " x-vss-e2eid: " + xVssHeader + ".";
                        }
                        if (xMsEdgeRefHeader != null) {
                            message += " x-msedge-ref: " + xMsEdgeRefHeader + ".";
                        }
                        message
                            += "For troubleshooting information see https://aka.ms/azsdk/java/identity/azurepipelinescredential/troubleshoot.";
                        throw LOGGER.throwableAtError().log(message, CredentialAuthenticationException::new);
                    }
                    try (JsonReader reader = JsonReader.fromString(responseBody)) {
                        return OidcTokenResponse.fromJson(reader).getOidcToken();
                    }
                }
            } catch (IOException e) {
                throw LOGGER.throwableAtError()
                    .log("Failed to get the client assertion token", e, CredentialAuthenticationException::new);
            }
        });
        this.confidentialClient = new ConfidentialClient(confidentialClientOptions);
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            AccessToken token = confidentialClient.authenticateWithCache(request);
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (RuntimeException ignored) {
        }

        try {
            AccessToken token = confidentialClient.authenticate(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (RuntimeException e) {
            throw logAndThrowTokenError(LOGGER, request, e, CoreException::from);
        }
    }
}
