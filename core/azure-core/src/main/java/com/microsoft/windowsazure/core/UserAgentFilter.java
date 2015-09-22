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
package com.microsoft.windowsazure.core;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class UserAgentFilter.
 */
public class UserAgentFilter implements ServiceRequestFilter {

    /** The azure SDK product token. */
    private static String azureSDKProductToken;

    /**
     * Instantiates a new user agent filter.
     */
    public UserAgentFilter() {
        if ((azureSDKProductToken == null) || azureSDKProductToken.isEmpty()) {
            azureSDKProductToken = createAzureSDKProductToken();
        }
    }

    @Override
    public void filter(ServiceRequestContext request) {
        String userAgent;

        if (request.getHeader("User-Agent") != null) {
            String currentUserAgent = request.getHeader("User-Agent");
            userAgent = azureSDKProductToken + " " + currentUserAgent;
            request.removeHeader("User-Agent");
        } else {
            userAgent = azureSDKProductToken;
        }

        request.setHeader("User-Agent", userAgent);
    }

    /**
     * Creates the azure SDK product token.
     * 
     * @return the string
     */
    private String createAzureSDKProductToken() {
        String version = getVersionFromResources();
        String productToken;
        if ((version != null) && (!version.isEmpty())) {
            productToken = "Azure-SDK-For-Java/" + version;
        } else {
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
        String version = null;
        Properties properties = new Properties();
        try {
            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                            "META-INF/maven/com.microsoft.azure/azure-core/pom.properties");
            if (inputStream != null) {
                properties.load(inputStream);
                version = properties.getProperty("version");
                inputStream.close();
            }
        } catch (IOException e) {
            // Do nothing
        }

        return version;
    }
}
