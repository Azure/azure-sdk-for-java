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
package com.microsoft.windowsazure.services.servicebus;

import com.microsoft.windowsazure.Configuration;

/**
 * Provides functionality to create a service bus configuration.
 *
 */
public abstract class ServiceBusConfiguration {

    /**
     * Defines the configuration connection string constant.
     *
     */
    public static final String CONNECTION_STRING = "serviceBus.connection.string";

    /**
     * Defines the configuration URI constant.
     *
     */
    public static final String URI = "serviceBus.uri";

    /**
     * Defines the configuration wrap URI constant.
     *
     */
    public static final String WRAP_URI = "serviceBus.wrap.uri";

    /**
     * Defines the configuration wrap name constant.
     *
     */
    public static final String WRAP_NAME = "serviceBus.wrap.name";

    /**
     * Defines the configuration wrap password constant.
     *
     */
    public static final String WRAP_PASSWORD = "serviceBus.wrap.password";

    /**
     * Defines the configuration SAS Access Key name constant. This is the
     * value of the SharedAccessKeyName field in the connection string.
     */
    public static final String SAS_KEY_NAME = "serviceBus.sas.keyname";

    /**
     * Defines the configuration SAS Access key value constant. This is the actual token.
     */
    public static final String SAS_KEY = "serviceBus.sas.key";

    /**
     * Creates a service bus configuration using the specified namespace, name,
     * and password.
     *
     * @param namespace
     *            A <code>String</code> object that represents the namespace.
     *
     * @param authenticationName
     *            A <code>String</code> object that represents the
     *            authentication name.
     *
     * @param authenticationPassword
     *            A <code>String</code> object that represents the
     *            authentication password.
     *
     * @param serviceBusRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to connect
     *            to the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            ".servicebus.windows.net"
     *
     * @param wrapRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to get an
     *            access token for the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            "-sb.accesscontrol.windows.net/WRAPv0.9"
     *
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ServiceBusService</code> class.
     *
     */
    public static Configuration configureWithWrapAuthentication(
            String namespace, String authenticationName,
            String authenticationPassword, String serviceBusRootUri,
            String wrapRootUri) {
        return configureWithWrapAuthentication(null,
                Configuration.getInstance(), namespace, authenticationName,
                authenticationPassword, serviceBusRootUri, wrapRootUri);
    }

    /**
     * Creates a service bus configuration using the specified configuration,
     * namespace, name, and password.
     *
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     *
     * @param namespace
     *            A <code>String</code> object that represents the namespace.
     *
     * @param authenticationName
     *            A <code>String</code> object that represents the
     *            authentication name.
     *
     * @param authenticationPassword
     *            A <code>String</code> object that represents the
     *            authentication password.
     *
     * @param serviceBusRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to connect
     *            to the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            ".servicebus.windows.net"
     *
     * @param wrapRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to get an
     *            access token for the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            "-sb.accesscontrol.windows.net/WRAPv0.9"
     *
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ServiceBusService</code> class.
     *
     */
    public static Configuration configureWithWrapAuthentication(
            Configuration configuration, String namespace,
            String authenticationName, String authenticationPassword,
            String serviceBusRootUri, String wrapRootUri) {
        return configureWithWrapAuthentication(null, configuration, namespace,
                authenticationName, authenticationPassword, serviceBusRootUri,
                wrapRootUri);
    }

    /**
     * Creates a service bus configuration using the specified profile,
     * configuration, namespace, name, and password.
     *
     * @param profile
     *            A <code>String</code> object that represents the profile.
     *
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     *
     * @param namespace
     *            A <code>String</code> object that represents the namespace.
     *
     * @param authenticationName
     *            A <code>String</code> object that represents the
     *            authentication name.
     *
     * @param authenticationPassword
     *            A <code>String</code> object that represents the
     *            authentication password.
     *
     * @param serviceBusRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to connect
     *            to the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            ".servicebus.windows.net"
     *
     * @param wrapRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to get an
     *            access token for the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            "-sb.accesscontrol.windows.net/WRAPv0.9"
     *
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ServiceBusService</code> class.
     *
     */
    public static Configuration configureWithWrapAuthentication(String profile,
            Configuration configuration, String namespace,
            String authenticationName, String authenticationPassword,
            String serviceBusRootUri, String wrapRootUri) {

        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + CONNECTION_STRING, null);

        configuration.setProperty(profile + URI, "https://" + namespace
                + serviceBusRootUri);

        configuration.setProperty(profile + WRAP_URI, "https://" + namespace
                + wrapRootUri);

        configuration.setProperty(profile + WRAP_NAME, authenticationName);
        configuration.setProperty(profile + WRAP_PASSWORD,
                authenticationPassword);

        return configuration;
    }

    /**
     * Create a service bus configuration using the given SAS authentication information.
     * @param namespace
     *            A <code>String</code> object that represents the namespace.
     *
     * @param sasKeyName
     *            A <code>String</code> object that contains the SAS key name.
     *
     * @param sasKey
     *            A <code>String</code> object that contains the SAS key.
     *
     * @param serviceBusRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to connect
     *            to the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            ".servicebus.windows.net"
     *
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ServiceBusService</code> class.
     */
    public static Configuration configureWithSASAuthentication(
            String namespace,
            String sasKeyName, String sasKey,
            String serviceBusRootUri) {

        return configureWithSASAuthentication(null,
                Configuration.getInstance(),
                namespace, sasKeyName, sasKey, serviceBusRootUri);
    }

    /**
     * Create a service bus configuration using the given SAS authentication information.
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     *
     * @param namespace
     *            A <code>String</code> object that represents the namespace.
     *
     * @param sasKeyName
     *            A <code>String</code> object that contains the SAS key name.
     *
     * @param sasKey
     *            A <code>String</code> object that contains the SAS key.
     *
     * @param serviceBusRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to connect
     *            to the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            ".servicebus.windows.net"
     *
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ServiceBusService</code> class.
     */
    public static Configuration configureWithSASAuthentication(
            Configuration configuration, String namespace,
            String sasKeyName, String sasKey,
            String serviceBusRootUri) {
        return configureWithSASAuthentication(null, configuration,
                namespace, sasKeyName, sasKey, serviceBusRootUri);
    }

    /**
     * Create a service bus configuration using the given SAS authentication information.
     * @param profile
     *            A <code>String</code> object that represents the namespace.
     *
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     *
     * @param namespace
     *            A <code>String</code> object that represents the namespace.
     *
     * @param sasKeyName
     *            A <code>String</code> object that contains the SAS key name.
     *
     * @param sasKey
     *            A <code>String</code> object that contains the SAS key.
     *
     * @param serviceBusRootUri
     *            A <code>String</code> object containing the base URI that is
     *            added to your Service Bus namespace to form the URI to connect
     *            to the Service Bus service.
     *
     *            To access the default public Azure service, pass
     *            ".servicebus.windows.net"
     *
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ServiceBusService</code> class.
     */
    public static Configuration configureWithSASAuthentication(String profile,
            Configuration configuration, String namespace,
            String sasKeyName, String sasKey,
            String serviceBusRootUri) {
        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + CONNECTION_STRING, null);

        configuration.setProperty(profile + URI, "https://" + namespace
                + serviceBusRootUri);

        configuration.setProperty(profile + WRAP_URI, null);
        configuration.setProperty(profile + WRAP_NAME, null);
        configuration.setProperty(profile + WRAP_PASSWORD, null);

        configuration.setProperty(profile + SAS_KEY_NAME, sasKeyName);
        configuration.setProperty(profile + SAS_KEY, sasKey);

        return configuration;
    }

    /**
     * Creates a service bus configuration using the specified connection
     * string.
     *
     * @param profile
     *            A <code>String</code> object that represents the profile.
     * @param configuration
     *            A previously instantiated <code>Configuration</cone> object.
     * @param connectionString
     *            A <code>String</code> object the contains the connection
     *            string for the service bus connection information.
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>ServiceBusService</code> class.
     */
    public static Configuration configureWithConnectionString(String profile,
            Configuration configuration, String connectionString) {

        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration
                .setProperty(profile + CONNECTION_STRING, connectionString);

        return configuration;
    }
}
