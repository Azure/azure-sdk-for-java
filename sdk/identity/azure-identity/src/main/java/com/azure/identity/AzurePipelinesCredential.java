// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.identity.implementation.ClientAssertionCredentialHelper;
import com.azure.identity.implementation.IdentityClientBase;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;

/**
 * The {@link AzurePipelinesCredential} acquires a token using the Azure Devops Pipeline service connection.
 */
public class AzurePipelinesCredential implements TokenCredential {
    private final ClientAssertionCredentialHelper clientAssertionCredentialHelper;

    AzurePipelinesCredential(String clientId, String tenantId, String requestUrl, String systemAccessToken, IdentityClientOptions identityClientOptions) {


        clientAssertionCredentialHelper = new ClientAssertionCredentialHelper(clientId, tenantId, identityClientOptions, () -> {
            HttpClient client = identityClientOptions.getHttpClient();
            if (client == null ) {
                HttpClient.createDefault();
            }
            HttpPipeline pipeline = IdentityClientBase.setupPipeline(client, identityClientOptions);
            try {
                URL url = new URL(requestUrl);
                HttpRequest request = new HttpRequest(HttpMethod.POST, url);
                request.setHeader(HttpHeaderName.AUTHORIZATION, "Bearer " + systemAccessToken);
                request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
                try (HttpResponse response = pipeline.sendSync(request, null)) {
                    return response.getBodyAsString().block();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
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
