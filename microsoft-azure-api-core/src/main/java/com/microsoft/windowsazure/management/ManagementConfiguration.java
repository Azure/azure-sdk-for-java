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
package com.microsoft.windowsazure.management;

import com.microsoft.windowsazure.services.core.Configuration;

/**
 * Provides functionality to create a service management configuration.
 * 
 */
public class ManagementConfiguration {

    /**
     * Defines the path of the keystore.
     * 
     */
    public final static String KEYSTORE_PATH = "AZURE_MANAGEMENT_KEYSTORE_PATH";

    /**
     * Defines the password of the keystore.
     * 
     */
    public final static String KEYSTORE_PASSWORD = "AZURE_MANAGEMENT_KEYSTORE_PASSWORD";

    /**
     * Defines the type of the keystore.
     */
    public static final String KEYSTORE_TYPE = "AZURE_MANAGEMENT_KEYSTORE_TYPE";

    /**
     * Defines the URI of service management.
     * 
     */
    public final static String URI = "AZURE_MANAGEMENT_URI";

    /**
     * Defines the subscription ID of the Windows Azure account.
     */
    public static final String SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";
    
    public static final String SUBSCRIPTION_CLOUD_CREDENTIALS = "AZURE_SUBSCRIPTION_CLOUD_CREDENTIALS";

    /**
     * Creates a service management configuration using specified URI, and subscription ID.
     * 
     * @param uri
     *            A <code>String</code> object that represents the root URI of the service management service.
     * @param subscriptionId
     *            A <code>String</code> object that represents the subscription ID.
     * @return the configuration
     *         A <code>Configuration</code> object that can be used when creating an instance of the
     *         <code>ManagementContract</code> class.
     */
    public static Configuration configure(String uri, String subscriptionId) {
        return configure(null, Configuration.getInstance(), uri, subscriptionId, null, null);
    }

    /**
     * Creates a service management configuration with specified parameters.
     * 
     * @param profile
     *            A <code>String</code> object that represents the profile.
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     * @param uri
     *            A <code>String</code> object that represents the URI of the service management service.
     * @param subscriptionId
     *            A <code>String</code> object that represents the subscription ID.
     * @param keyStoreLocation
     *            the key store location
     * @param keyStorePassword
     *            A <code>String</code> object that represents the password of the keystore.
     * @return A <code>Configuration</code> object that can be used when creating an instance of the
     *         <code>ManagementContract</code> class.
     */
    public static Configuration configure(String profile, Configuration configuration, String uri,
            String subscriptionId, String keyStoreLocation, String keyStorePassword) {

        if (profile == null) {
            profile = "";
        }
        else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + URI, "https://" + uri);
        configuration.setProperty(profile + SUBSCRIPTION_ID, subscriptionId);
        configuration.setProperty(profile + KEYSTORE_PATH, keyStoreLocation);
        configuration.setProperty(profile + KEYSTORE_PASSWORD, keyStorePassword);
        
        configuration.setProperty(profile + SUBSCRIPTION_CLOUD_CREDENTIALS,
                new CertificateCloudCredentials(subscriptionId));
        
        return configuration;
    }
}