/**
 * Copyright 2012 Microsoft Corporation
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

import java.net.URISyntaxException;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Filter responsible for adding SAS tokens to outgoing requests.
 * 
 */
public class SASTokenFilter extends ClientFilter {
    private final String sasToken;

    /**
     * Construct a SASTokenFilter that will insert the tokens given
     * in the provided sasUrl.
     * 
     * @param sasUrl
     *            URL containing authentication information
     * @throws URISyntaxException
     */
    public SASTokenFilter(String sasToken) {
        this.sasToken = sasToken;
    }

    /* (non-Javadoc)
     * @see com.sun.jersey.api.client.filter.ClientFilter#handle(com.sun.jersey.api.client.ClientRequest)
     */
    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        UriBuilder newUri = UriBuilder.fromUri(cr.getURI());
        String currentQuery = cr.getURI().getRawQuery();
        if (currentQuery == null) {
            currentQuery = "";
        }
        else if (currentQuery.length() > 0) {
            currentQuery += "&";
        }
        currentQuery += sasToken;

        newUri.replaceQuery(currentQuery);
        cr.setURI(newUri.build());

        return getNext().handle(cr);
    }

}
