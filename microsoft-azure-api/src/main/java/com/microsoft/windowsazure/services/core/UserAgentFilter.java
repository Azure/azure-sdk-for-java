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
package com.microsoft.windowsazure.services.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * The Class UserAgentFilter.
 */
public class UserAgentFilter extends ClientFilter {

    /** The azure sdk product token. */
    private static String azureSDKProductToken;

    /**
     * Instantiates a new user agent filter.
     */
    public UserAgentFilter() {
        if ((azureSDKProductToken == null) || azureSDKProductToken.isEmpty()) {
            azureSDKProductToken = createAzureSDKProductToken();
        }

    }

    /* (non-Javadoc)
     * @see com.sun.jersey.api.client.filter.ClientFilter#handle(com.sun.jersey.api.client.ClientRequest)
     */
    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
        String userAgent;

        if (clientRequest.getHeaders().containsKey("User-Agent")) {
            String currentUserAgent = (String) clientRequest.getHeaders().getFirst("User-Agent");
            userAgent = azureSDKProductToken + " " + currentUserAgent;
            clientRequest.getHeaders().remove("User-Agent");
        }
        else {
            userAgent = azureSDKProductToken;
        }

        clientRequest.getHeaders().add("User-Agent", userAgent);

        return this.getNext().handle(clientRequest);
    }

    /**
     * Creates the azure sdk product token.
     * 
     * @return the string
     */
    private String createAzureSDKProductToken() {
        String version = getVersionFromResources();
        String productToken;
        if ((version != null) && (!version.isEmpty())) {
            productToken = "Azure-SDK-For-Java/" + version;
        }
        else {
            productToken = "Azure-SDK-For-Java";
        }

        return productToken;
    }

    /**
     * Gets the version of the SDK from resources.
     * 
     * @return the version from resources
     */
    private String getVersionFromResources() {
        String version = "unknown";
        Properties properties = new Properties();
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                    "META-INF/maven/com.microsoft.windowsazure/microsoft-windowsazure-api/pom.properties");
            if (inputStream != null) {
                properties.load(inputStream);
                version = properties.getProperty("version");
                inputStream.close();
            }
        }
        catch (IOException e) {
        }

        return version;
    }
}
