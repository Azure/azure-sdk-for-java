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

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.core.pipeline.jersey.IdempotentClientFilter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

/**
 * A small filter that adds the required Media services/OData 3 version headers
 * to the request as it goes through.
 * 
 */
public class VersionHeadersFilter extends IdempotentClientFilter {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.core.IdempotentClientFilter#doHandle
     * (com.sun.jersey.api.client.ClientRequest)
     */
    @Override
    public ClientResponse doHandle(ClientRequest cr) {
        MultivaluedMap<String, Object> headers = cr.getHeaders();
        headers.add("DataServiceVersion", "3.0");
        headers.add("MaxDataServiceVersion", "3.0");
        headers.add("x-ms-version", "2.17");
        return getNext().handle(cr);
    }
}
