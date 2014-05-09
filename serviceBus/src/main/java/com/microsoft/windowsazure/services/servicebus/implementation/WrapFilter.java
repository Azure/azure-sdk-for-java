/**
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
package com.microsoft.windowsazure.services.servicebus.implementation;

import com.microsoft.windowsazure.exception.ServiceException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class WrapFilter extends ClientFilter {
    private final WrapTokenManager tokenManager;

    public WrapFilter(WrapTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) {
        String accessToken = getWrapToken(cr.getURI());
        cr.getHeaders().add("Authorization", accessToken);

        String secondaryAuthorizationUri = (String) cr.getHeaders().getFirst(
                "ServiceBusSupplementaryAuthorization");
        if ((secondaryAuthorizationUri != null)
                && (!secondaryAuthorizationUri.isEmpty())) {
            String secondaryAccessToken = getWrapToken(URI
                    .create(secondaryAuthorizationUri));
            cr.getHeaders().remove("ServiceBusSupplementaryAuthorization");
            cr.getHeaders().add("ServiceBusSupplementaryAuthorization",
                    secondaryAccessToken);
        }

        return this.getNext().handle(cr);
    }

    private String getWrapToken(URI uri) {
        String result;
        try {
            result = tokenManager.getAccessToken(uri);
        } catch (ServiceException e) {
            // must wrap exception because of base class signature
            throw new ClientHandlerException(e);
        } catch (URISyntaxException e) {
            // must wrap exception because of base class signature
            throw new ClientHandlerException(e);
        }

        return "WRAP access_token=\"" + result + "\"";
    }
}
