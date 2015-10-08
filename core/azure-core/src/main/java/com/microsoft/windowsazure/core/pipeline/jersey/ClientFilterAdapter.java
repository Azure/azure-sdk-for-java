/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.core.pipeline.jersey;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ClientFilterAdapter extends ClientFilter {
    private ServiceFilter filter;

    public ClientFilterAdapter(ServiceFilter filter) {
        this.filter = filter;
    }

    @Override
    public ClientResponse handle(ClientRequest clientRequest) {

        final ClientRequest cr = clientRequest;
        try {
            ServiceResponseContext resp = filter.handle(
                    new JerseyServiceRequestContext(clientRequest),
                    new ServiceFilter.Next() {
                        @Override
                        public ServiceResponseContext handle(
                                ServiceRequestContext request) {
                            return new JerseyServiceResponseContext(getNext()
                                    .handle(cr));
                        }
                    });

            return ((JerseyServiceResponseContext) resp).getClientResponse();
        } catch (Exception e) {
            throw new ClientHandlerException(e);
        }
    }
}