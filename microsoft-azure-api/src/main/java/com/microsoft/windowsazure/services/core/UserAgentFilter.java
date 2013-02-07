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
import java.net.URLClassLoader;
import java.util.Properties;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class UserAgentFilter extends ClientFilter {

    private final String azureSDKProductToken;

    public UserAgentFilter() {
        String version = getVersionFromResources();
        azureSDKProductToken = "Azure SDK for Java/" + version;

    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        String userAgent;
        String currentUserAgent = (String) cr.getHeaders().getFirst("User-Agent");

        if (currentUserAgent != null) {
            userAgent = azureSDKProductToken + " " + currentUserAgent;
        }
        else {
            userAgent = azureSDKProductToken;
        }

        cr.getHeaders().remove("User-Agent");
        cr.getHeaders().add("User-Agent", userAgent);

        return this.getNext().handle(cr);
    }

    private String getVersionFromResources() {
        String version;
        Properties properties = new Properties();
        URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
        try {
            InputStream inputStream = classLoader
                    .getResourceAsStream("META-INF/maven/com.microsoft.windowsazure/microsoft-windowsazure-api/pom.properties");
            properties.load(inputStream);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        version = properties.getProperty("version");
        return version;
    }
}
