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

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public abstract class AuthorizationFilter extends ClientFilter {
    @Override
    public ClientResponse handle(ClientRequest cr) {
        String accessToken = createAuthorization(cr.getURI().toString());
        cr.getHeaders().add("Authorization", accessToken);

        String secondaryAuthorizationUri = (String) cr.getHeaders().getFirst(
                "ServiceBusSupplementaryAuthorization");
        if ((secondaryAuthorizationUri != null)
                && (!secondaryAuthorizationUri.isEmpty())) {
            String secondaryAccessToken =
                    createAuthorization(secondaryAuthorizationUri);
            cr.getHeaders().remove("ServiceBusSupplementaryAuthorization");
            cr.getHeaders().add("ServiceBusSupplementaryAuthorization",
                    secondaryAccessToken);
        }

        return this.getNext().handle(cr);
    }

    protected abstract String createAuthorization(String targetUri);
}
