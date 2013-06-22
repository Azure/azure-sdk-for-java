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
package com.microsoft.windowsazure.services.management;

import com.microsoft.windowsazure.services.core.Configuration;

/**
 * Provides functionality to create a service bus configuration.
 * 
 */
public class ManagementConfiguration {

    /**
     * Defines the location of the keystore.
     * 
     */
    public final static String KEYSTORE_PATH = "management.keystore.path";

    /**
     * Defines the password of the keystore.
     * 
     */
    public final static String KEYSTORE_PASSWORD = "management.keystore.password";

    /**
     * Defines the URI of service management.
     * 
     */
    public final static String URI = "management.uri";

    /**
     * Defines the subscription ID of the service management.
     */
    public static final String SUBSCRIPTION_ID = "management.subscription.id";

    /**
     * Creates a service management configuration using the specified uri, keystore path, and subscription id.
     * 
     * 
     * @param uri
     *            A <code>String</code> object that represents the URI.
     * 
     * @param subscriptionId
     *            A <code>String</code> object that represents the subscription ID.
     * 
     * @param keystorePath
     *            A <code>String</code> object that represents the path of the keystore.
     * 
     * @return
     *         A <code>Configuration</code> object that can be used when creating an instance of the
     *         <code>ManagementService</code> class.
     * 
     */
    public static Configuration configure(String uri, String subscriptionId) {
        return configure(null, Configuration.getInstance(), uri, subscriptionId, null, null);
    }

    public static Configuration configure(String uri, String subscriptionId, String keyStorePath,
            String keyStorePassword) {
        return configure(null, Configuration.getInstance(), uri, subscriptionId, keyStorePath, keyStorePassword);
    }

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

        return configuration;
    }

}
