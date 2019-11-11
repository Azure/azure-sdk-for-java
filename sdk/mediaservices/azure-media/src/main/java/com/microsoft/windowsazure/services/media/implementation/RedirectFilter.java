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

package com.microsoft.windowsazure.services.media.implementation;

import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.core.pipeline.jersey.IdempotentClientFilter;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

public class RedirectFilter extends IdempotentClientFilter {
    private final ResourceLocationManager locationManager;

    public RedirectFilter(ResourceLocationManager locationManager) {
        this.locationManager = locationManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.core.IdempotentClientFilter#doHandle
     * (com.sun.jersey.api.client.ClientRequest)
     */
    @Override
    public ClientResponse doHandle(ClientRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request should not be null");
        }

        URI originalURI = request.getURI();
        request.setURI(locationManager.getRedirectedURI(originalURI));

        ClientResponse response = getNext().handle(request);
        while (response.getClientResponseStatus() == ClientResponse.Status.MOVED_PERMANENTLY) {
            try {
                locationManager.setRedirectedURI(response.getHeaders()
                        .getFirst("Location"));
            } catch (NullPointerException e) {
                throw new ClientHandlerException(
                        "HTTP Redirect did not include Location header");
            } catch (URISyntaxException e) {
                throw new ClientHandlerException(
                        "HTTP Redirect location is not a valid URI");
            }

            request.setURI(locationManager.getRedirectedURI(originalURI));
            response = getNext().handle(request);
        }
        return response;
    }

    public URI getBaseURI() {
        return this.locationManager.getBaseURI();
    }
}
