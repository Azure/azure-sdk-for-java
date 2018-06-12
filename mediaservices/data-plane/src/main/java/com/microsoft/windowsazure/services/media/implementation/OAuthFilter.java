/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.media.implementation;

import javax.inject.Named;

import com.microsoft.windowsazure.core.pipeline.jersey.IdempotentClientFilter;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.authentication.AzureAdAccessToken;
import com.microsoft.windowsazure.services.media.authentication.TokenProvider;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

/**
 * The Jersey filter for OAuth.
 *
 */
public class OAuthFilter extends IdempotentClientFilter {
    private final TokenProvider azureAdTokenProvider;

    /**
     * Creates an <code>OAuthFilter</code> object with specified
     * <code>TokenProvider</code> instance.
     *
     * @param azureAdTokenProvider
     */
    public OAuthFilter(@Named(MediaConfiguration.AZURE_AD_TOKEN_PROVIDER) TokenProvider azureAdTokenProvider) {
        this.azureAdTokenProvider = azureAdTokenProvider;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.microsoft.windowsazure.services.core.IdempotentClientFilter#doHandle
     * (com.sun.jersey.api.client.ClientRequest)
     */
    @Override
    public ClientResponse doHandle(ClientRequest clientRequest) {
        AzureAdAccessToken accessToken;

        try {
            accessToken = azureAdTokenProvider.acquireAccessToken();
        } catch (Exception e) {
            throw new ClientHandlerException("Failed to acquire access token", e);
        }

        clientRequest.getHeaders().add("Authorization", "Bearer " + accessToken.getAccessToken());

        return this.getNext().handle(clientRequest);
    }
}
