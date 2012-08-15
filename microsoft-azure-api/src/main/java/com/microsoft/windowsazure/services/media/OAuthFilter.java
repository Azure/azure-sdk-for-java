/*
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.media;

import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * The Jersey filter for OAuth.
 * 
 * @author azurejava@microsoft.com
 * 
 */
public class OAuthFilter extends ClientFilter {
    private final OAuthTokenManager oAuthTokenManager;

    /**
     * Creates an <code>OAuthFilter</code> object with specified <code>OAuthTokenManager</code> instance.
     * 
     * @param oAuthTokenManager
     */
    public OAuthFilter(OAuthTokenManager oAuthTokenManager) {
        this.oAuthTokenManager = oAuthTokenManager;
    }

    /**
     * Handles response with a specified client request.
     * 
     * @param clientRequest
     *            A <code>ClientRequest</code> object representing a client request.
     */
    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {

        String accessToken;
        try {
            accessToken = oAuthTokenManager.getAccessToken();
        }
        catch (ServiceException e) {
            // must wrap exception because of base class signature
            throw new ClientHandlerException(e);
        }
        catch (URISyntaxException e) {
            // must wrap exception because of base class signature
            throw new ClientHandlerException(e);
        }

        clientRequest.getHeaders().add("Authorization", "Bearer " + accessToken);

        return this.getNext().handle(clientRequest);
    }
}
