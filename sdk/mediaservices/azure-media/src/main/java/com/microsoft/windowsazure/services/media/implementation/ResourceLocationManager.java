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

import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import com.microsoft.windowsazure.services.media.MediaConfiguration;

public class ResourceLocationManager {
    private URI baseURI;

    public ResourceLocationManager(@Named(MediaConfiguration.AZURE_AD_API_SERVER) String baseUri)
            throws URISyntaxException {
        this.baseURI = new URI(baseUri);
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public URI getRedirectedURI(URI originalURI) {
        UriBuilder uriBuilder = UriBuilder.fromUri(baseURI).path(
                originalURI.getPath());
        String queryString = originalURI.getRawQuery();

        if (queryString != null && !queryString.isEmpty()) {
            uriBuilder.replaceQuery(queryString);
        }
        return uriBuilder.build();
    }

    public void setRedirectedURI(String newURI) throws URISyntaxException {
        baseURI = new URI(newURI);
    }
}
