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

import com.microsoft.windowsazure.core.utils.KeyStoreCredential;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.credentials.CertificateCloudCredentials;
import com.microsoft.windowsazure.Configuration;
import java.io.IOException;
import java.net.URI;

/**
 * Provides functionality to create a service management configuration.
 * 
 */
public final class ManagementConfiguration {
    private ManagementConfiguration() {
    }

    /**
     * Defines the subscription cloud credentials object of the Windows Azure
     * account.
     */
    public static final String SUBSCRIPTION_CLOUD_CREDENTIALS = "com.microsoft.windowsazure.Configuration.credentials";

    /**
     * Defines the path of the keystore.
     * 
     */
    public static final String KEYSTORE_PATH = "management.keystore.path";

    /**
     * Defines the password of the keystore.
     * 
     */
    public static final String KEYSTORE_PASSWORD = "management.keystore.password";

    /**
     * Defines the type of the keystore.
     */
    public static final String KEYSTORE_TYPE = "management.keystore.type";

    /**
     * Defines the URI of service management.
     * 
     */
    public static final String URI = "management.uri";

    /**
     * Defines the subscription ID of the Windows Azure account.
     */
    public static final String SUBSCRIPTION_ID = "management.subscription.id";

    /**
     * Creates a service management configuration using specified URI, and
     * subscription ID.
     * 
     * @param subscriptionId
     *            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation
     *            A <code>String</code> object that represents the key store
     *            location.
     * @param keyStorePassword
     *            A <code>String</code> object that represents the key store
     *            password.
     * @return the configuration A <code>Configuration</code> object that can be
     *         used when creating an instance of the
     *         <code>ManagementContract</code> class.
     * @throws java.io.IOException
     *             If the key store location or its contents is invalid.
     */
    public static Configuration configure(URI uri, String subscriptionId,
            String keyStoreLocation, String keyStorePassword)
            throws IOException {
        return configure(null, Configuration.getInstance(), uri, subscriptionId,
                keyStoreLocation, keyStorePassword);
    }

    /**
     * Creates a service management configuration using specified URI, and
     * subscription ID.
     * 
     * @param subscriptionId
     *            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation
     *            A <code>String</code> object that represents the key store
     *            location.
     * @param keyStorePassword
     *            A <code>String</code> object that represents the key store
     *            password.
     * @param type
     *            Type of key store.
     * @return the configuration A <code>Configuration</code> object that can be
     *         used when creating an instance of the
     *         <code>ManagementContract</code> class.
     * @throws java.io.IOException
     *             If the key store location or its contents is invalid.
     */
    public static Configuration configure(URI uri, String subscriptionId,
            String keyStoreLocation, String keyStorePassword, KeyStoreType type)
            throws IOException {
        return configure(null, Configuration.getInstance(), uri, subscriptionId,
                keyStoreLocation, keyStorePassword, type);
    }

    /**
     * Creates a service management configuration with specified parameters.
     * 
     * @param profile
     *            A <code>String</code> object that represents the profile.
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     * @param subscriptionId
     *            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation
     *            the key store location
     * @param keyStorePassword
     *            A <code>String</code> object that represents the password of
     *            the keystore.
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ManagementContract</code>
     *         class.
     * @throws java.io.IOException
     *             If the key store location or its contents is invalid.
     */
    public static Configuration configure(String profile,
            Configuration configuration, URI uri, String subscriptionId,
            String keyStoreLocation, String keyStorePassword)
            throws IOException {

        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + SUBSCRIPTION_ID, subscriptionId);
        configuration.setProperty(profile + KEYSTORE_PATH, keyStoreLocation);
        configuration
                .setProperty(profile + KEYSTORE_PASSWORD, keyStorePassword);

        configuration.setProperty(profile + SUBSCRIPTION_CLOUD_CREDENTIALS,
                new CertificateCloudCredentials(uri, subscriptionId,
                        new KeyStoreCredential(keyStoreLocation,
                                keyStorePassword)));

        return configuration;
    }

    /**
     * Creates a service management configuration with specified parameters.
     * 
     * @param profile
     *            A <code>String</code> object that represents the profile.
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     * @param subscriptionId
     *            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation
     *            the key store location
     * @param keyStorePassword
     *            A <code>String</code> object that represents the password of
     *            the keystore.
     * @param keyStoreType
     *            The type of key store.
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ManagementContract</code>
     *         class.
     * @throws java.io.IOException
     *             If the key store location or its contents is invalid.
     */
    public static Configuration configure(String profile,
            Configuration configuration, URI uri, String subscriptionId,
            String keyStoreLocation, String keyStorePassword, KeyStoreType keyStoreType)
            throws IOException {

        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + ManagementConfiguration.URI, uri);
        configuration.setProperty(profile + ManagementConfiguration.SUBSCRIPTION_ID, subscriptionId);
        configuration.setProperty(profile + ManagementConfiguration.KEYSTORE_PATH, keyStoreLocation);
        configuration.setProperty(profile + ManagementConfiguration.KEYSTORE_PASSWORD, keyStorePassword);
        configuration.setProperty(profile + ManagementConfiguration.KEYSTORE_TYPE, keyStoreType);

        KeyStoreCredential keyStoreCredential = new KeyStoreCredential(
                keyStoreLocation, keyStorePassword, keyStoreType);
        CertificateCloudCredentials cloudCredentials = new CertificateCloudCredentials(uri,
                subscriptionId, keyStoreCredential);
        configuration.setProperty(profile + SUBSCRIPTION_CLOUD_CREDENTIALS,
                cloudCredentials);

        return configuration;
    }
}