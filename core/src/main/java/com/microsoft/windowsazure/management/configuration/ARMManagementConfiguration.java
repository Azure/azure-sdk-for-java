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
package com.microsoft.windowsazure.management.configuration;

import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.credentials.AdalAuthConfig;
import com.microsoft.windowsazure.credentials.TokenCloudCredentials;
import com.microsoft.windowsazure.Configuration;

import java.io.IOException;
import java.net.URI;

import org.apache.http.impl.client.LaxRedirectStrategy;

/**
 * Provides functionality to create a service management configuration.
 * 
 */
public final class ARMManagementConfiguration {
    
    /**
     * Instantiates a new management configuration.
     */
    private ARMManagementConfiguration() {
    }

    /**
     * Defines the subscription cloud credentials object of the Windows Azure
     * account.
     */
    public static final String SUBSCRIPTION_CLOUD_CREDENTIALS = "com.microsoft.windowsazure.Configuration.credentials";

    /**
     * Defines the URI of service management.
     * 
     */
    public static final String URI = "management.uri";

    /**
     * Defines the subscription ID of the Windows Azure account.
     */
    public static final String SUBSCRIPTION_ID = "management.subscription.id";

    public static Configuration configure(String profile,
            Configuration configuration, URI uri, String subscriptionId,
            String username, String password)
            throws IOException {
    	return null;
    }
    
    /**
     * Creates a service management configuration with specified parameters.
     *
     * @param profile            A <code>String</code> object that represents the profile.
     * @param configuration            A previously instantiated <code>Configuration</code> object.
     * @param uri            A <code>URI</code> object that represents the URI of the 
     *            service end point.
     * @param subscriptionId            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation            the key store location
     * @param keyStorePassword            A <code>String</code> object that represents the password of
     *            the keystore.
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ManagementContract</code>
     *         class.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Configuration configure(String profile,
            Configuration configuration, URI uri, String subscriptionId,
            String username, String password,
            String authorityUrl, String tenantId, String resourceId,
            String clientId, String clientSecret)
            throws IOException {

        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + SUBSCRIPTION_ID, subscriptionId);

        configuration.setProperty(profile + SUBSCRIPTION_CLOUD_CREDENTIALS,
                new TokenCloudCredentials(uri, subscriptionId,
                        new AdalAuthConfig(authorityUrl, tenantId, resourceId, clientId, clientSecret)));

        configuration.setProperty(profile + ApacheConfigurationProperties.PROPERTY_REDIRECT_STRATEGY,
                new LaxRedirectStrategy());
        
        return configuration;
    }
}