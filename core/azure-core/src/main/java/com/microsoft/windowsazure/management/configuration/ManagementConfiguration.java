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

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreCredential;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.credentials.CertificateCloudCredentials;
import com.microsoft.windowsazure.credentials.TokenCloudCredentials;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.IOException;
import java.net.URI;

/**
 * Provides functionality to create a service management configuration.
 * 
 */
public final class ManagementConfiguration {
    
    /**
     * Instantiates a new management configuration.
     */
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
     * Defines the if the tests are run mocked.
     * 
     */
    public static final String AZURE_TEST_MODE = "test.mode";

    /**
     * Defines the subscription ID of the Windows Azure account.
     */
    public static final String SUBSCRIPTION_ID = "management.subscription.id";
    
    /**
     * Defines the cloud service name for the scheduler. 
     */
    public static final String CLOUD_SERVICE_NAME = "management.cloud.service.name";
    
    /**
     * Defines the job collection name for the scheduler.
     */
    public static final String JOB_COLLECTION_NAME = "management.job.collection.name";
    
    /**
     * Creates a service management configuration using specified URI, and
     * subscription ID.
     *
     * @param uri            A <code>URI</code> object that represents the URI of the 
     *            service end point. 
     * @param subscriptionId            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation            A <code>String</code> object that represents the key store
     *            location.
     * @param keyStorePassword            A <code>String</code> object that represents the key store
     *            password.
     * @return the configuration A <code>Configuration</code> object that can be
     *         used when creating an instance of the
     *         <code>ManagementContract</code> class.
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @param uri the uri
     * @param subscriptionId            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation            A <code>String</code> object that represents the key store
     *            location.
     * @param keyStorePassword            A <code>String</code> object that represents the key store
     *            password.
     * @param type            Type of key store.
     * @return the configuration A <code>Configuration</code> object that can be
     *         used when creating an instance of the
     *         <code>ManagementContract</code> class.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Configuration configure(URI uri, String subscriptionId,
            String keyStoreLocation, String keyStorePassword, KeyStoreType type)
            throws IOException {
        return configure(null, Configuration.getInstance(), uri, subscriptionId,
                keyStoreLocation, keyStorePassword, type);
    }

    /**
     * Creates a service management configuration using specified URI, and
     * subscription ID.
     *
     * @param uri
     *            A <code>URI</code> object that represents URI of the service
     *            end point.
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
     * @param cloudServiceName for
     *            A <code>String</code> object that represents the cloud service name
     *            for scheduler job.
     * @param jobCollectionName
     *            A <code>String</code> object that represents the job collection
     *            name for scheduler job.
     * @return the configuration A <code>Configuration</code> object that can be
     *         used when creating an instance of the
     *         <code>ManagementContract</code> class.
     * @throws java.io.IOException
     *             If the key store location or its contents is invalid.
     */
    public static Configuration configure(URI uri, String subscriptionId,
            String keyStoreLocation, String keyStorePassword, KeyStoreType type, String cloudServiceName, String jobCollectionName)
            throws IOException {
        return configure(null, Configuration.getInstance(), uri, subscriptionId,
                keyStoreLocation, keyStorePassword, type, cloudServiceName, jobCollectionName);
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

        configuration.setProperty(profile + ApacheConfigurationProperties.PROPERTY_REDIRECT_STRATEGY,
                new LaxRedirectStrategy());
        
        return configuration;
    }

    /**
     * Creates a service management configuration with specified parameters.
     *
     * @param profile            A <code>String</code> object that represents the profile.
     * @param configuration            A previously instantiated <code>Configuration</code> object.
     * @param uri            A <code>URI</code> object that represents the URI of the service
     *            end point.
     * @param subscriptionId            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation            the key store location
     * @param keyStorePassword            A <code>String</code> object that represents the password of
     *            the keystore.
     * @param keyStoreType            The type of key store.
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ManagementContract</code>
     *         class.
     * @throws IOException Signals that an I/O exception has occurred.
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
        
        configuration.setProperty(profile + ApacheConfigurationProperties.PROPERTY_REDIRECT_STRATEGY,
                new LaxRedirectStrategy());

        return configuration;
    }
    
    /**
     * Creates a service management configuration for the scheduler.
     *
     * @param profile            A <code>String</code> object that represents the profile.
     * @param configuration            A previously instantiated <code>Configuration</code> object.
     * @param uri            A <code>URI</code> object that represents the URI of the service
     *            end point.
     * @param subscriptionId            A <code>String</code> object that represents the subscription
     *            ID.
     * @param keyStoreLocation            A <code>String</code> object that represents the key store location
     * @param keyStorePassword            A <code>String</code> object that represents the password of
     *            the keystore.
     * @param keyStoreType            The type of key store.
     * @param cloudServiceName            A <code>String</code> object that represents the name of the cloud service. 
     * @param jobCollectionName           A <code>String</code> object that represents the name of the job collection. 
     *            
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ManagementContract</code>
     *         class.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Configuration configure(String profile,
    Configuration configuration, URI uri, String subscriptionId,
    String keyStoreLocation, String keyStorePassword, KeyStoreType keyStoreType, String cloudServiceName, String jobCollectionName) throws IOException
    {
        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }
        
        Configuration resultConfiguration = configure(profile, configuration, uri, subscriptionId, keyStoreLocation, keyStorePassword, keyStoreType);
        resultConfiguration.setProperty(profile+ManagementConfiguration.CLOUD_SERVICE_NAME, cloudServiceName);
        resultConfiguration.setProperty(profile+ManagementConfiguration.JOB_COLLECTION_NAME, jobCollectionName);
        return resultConfiguration;
    }

    public static Configuration configure(String profile, URI uri, String subscriptionId,
                                          String token)
            throws IOException {
        return configure(profile, Configuration.getInstance(), uri, subscriptionId, token);
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
     *            the keystore.
     * @param token          The authentication token
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ManagementContract</code>
     *         class.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Configuration configure(String profile,
                                          Configuration configuration, URI uri, String subscriptionId,
                                          String token)
            throws IOException {

        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + SUBSCRIPTION_ID, subscriptionId);

        configuration.setProperty(profile + SUBSCRIPTION_CLOUD_CREDENTIALS,
                new TokenCloudCredentials(uri, subscriptionId, token));

        configuration.setProperty(profile + ApacheConfigurationProperties.PROPERTY_REDIRECT_STRATEGY,
                new LaxRedirectStrategy());

        return configuration;
    }

    /**
     * Check current test mode (record/playback)
     * @return Current test mode is playback
     */
    public static boolean isPlayback() {
        return System.getenv(ManagementConfiguration.AZURE_TEST_MODE) != null &&
                System.getenv(ManagementConfiguration.AZURE_TEST_MODE).equals("playback");
    }
}