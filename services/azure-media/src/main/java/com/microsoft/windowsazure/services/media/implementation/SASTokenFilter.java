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

import javax.ws.rs.core.UriBuilder;

import com.microsoft.windowsazure.core.pipeline.jersey.IdempotentClientFilter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Filter responsible for adding SAS tokens to outgoing requests.
 * 
 */
public class SASTokenFilter extends IdempotentClientFilter {
    private final String sasToken;

    /**
     * Construct a SASTokenFilter that will insert the tokens given in the
     * provided sasUrl.
     * 
     * @param sasUrl
     *            URL containing authentication information
     */
    public SASTokenFilter(String sasToken) {
        this.sasToken = sasToken;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.core.IdempotentClientFilter#doHandle
     * (com.sun.jersey.api.client.ClientRequest)
     */
    @Override
    public ClientResponse doHandle(ClientRequest cr) {
        UriBuilder newUri = UriBuilder.fromUri(cr.getURI());
        String currentQuery = cr.getURI().getRawQuery();
        if (currentQuery == null) {
            currentQuery = "";
        } else if (currentQuery.length() > 0) {
            currentQuery += "&";
        }
        currentQuery += "api-version=2016-05-31&" + sasToken;

        newUri.replaceQuery(currentQuery);
        cr.setURI(newUri.build());

        return getNext().handle(cr);
    }
}
