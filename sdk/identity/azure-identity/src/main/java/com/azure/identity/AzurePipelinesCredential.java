// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.ClientAssertionCredentialHelper;
import com.azure.identity.implementation.IdentityClientBase;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.models.OidcTokenResponse;
import com.azure.json.JsonProviders;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;

/**
 * The {@link AzurePipelinesCredential} acquires a token using the Azure Pipelines service connection.
 *
 * To construct an instance of this credential, use the {@link AzurePipelinesCredentialBuilder}:
 * <!-- src_embed com.azure.identity.credential.azurepipelinescredential.construct -->
 * <pre>
 * &#47;&#47; serviceConnectionId is retrieved from the portal.
 * &#47;&#47; systemAccessToken is retrieved from the pipeline environment as shown.
 * &#47;&#47; You may choose another name for this variable.
 *
 * String systemAccessToken = System.getenv&#40;&quot;SYSTEM_ACCESSTOKEN&quot;&#41;;
 * AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .serviceConnectionId&#40;serviceConnectionId&#41;
 *     .systemAccessToken&#40;systemAccessToken&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azurepipelinescredential.construct -->
 *
 */
@Immutable
public class AzurePipelinesCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzurePipelinesCredential.class);
    private final ClientAssertionCredentialHelper clientAssertionCredentialHelper;

    /**
     * Creates an instance of {@link AzurePipelinesCredential}.
     *
     * @param clientId the client id of the service principal
     * @param tenantId the tenant id of the service principal
     * @param requestUrl the request url to get the client assertion token
     * @param systemAccessToken the system access token
     * @param identityClientOptions the options for configuring the identity client
     */
    AzurePipelinesCredential(String clientId, String tenantId, String requestUrl, String systemAccessToken, IdentityClientOptions identityClientOptions) {
        clientAssertionCredentialHelper = new ClientAssertionCredentialHelper(clientId, tenantId, identityClientOptions, () -> {
            HttpClient client = identityClientOptions.getHttpClient();
            if (client == null) {
                HttpClient.createDefault();
            }
            HttpPipeline pipeline = IdentityClientBase.setupPipeline(client, identityClientOptions);
            try {
                URL url = new URL(requestUrl);
                HttpRequest request = new HttpRequest(HttpMethod.POST, url);
                request.setHeader(HttpHeaderName.AUTHORIZATION, "Bearer " + systemAccessToken);
                request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
                try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
                    if (response.getStatusCode() < 200 || response.getStatusCode() >= 400) {
                        throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Failed to get the client assertion token "
                            + response.getBodyAsString().block()
                            + System.lineSeparator()
                            + "For troubleshooting information see https://aka.ms/azsdk/java/identity/azurepipelinescredential/troubleshoot.", response));
                    }
                    OidcTokenResponse tokenResponse = OidcTokenResponse.fromJson(JsonProviders.createReader(response.getBodyAsString().block()));
                    return tokenResponse.getOidcToken();
                }
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Failed to get the client assertion token", null, e));
            }
        });
    }
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return clientAssertionCredentialHelper.getToken(request);
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        return clientAssertionCredentialHelper.getTokenSync(request);
    }
}
