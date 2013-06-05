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
     * Defines the location of the certificate.
     * 
     */
    public final static String CERTIFICATE_LOCATION = "certificate.location";

    /**
     * Defines the configuration URI constant.
     * 
     */
    public final static String URI = "management.uri";

    /**
     * Creates a service bus configuration using the specified namespace, name, and password.
     * 
     * @param namespace
     *            A <code>String</code> object that represents the namespace.
     * 
     * @param authenticationName
     *            A <code>String</code> object that represents the authentication name.
     * 
     * @param authenticationPassword
     *            A <code>String</code> object that represents the authentication password.
     * 
     * @param serviceBusRootUri
     *            A <code>String</code> object containing the base URI that is added to your
     *            Service Bus namespace to form the URI to connect to the Service Bus service.
     * 
     *            To access the default public Azure service, pass ".servicebus.windows.net"
     * 
     * @param wrapRootUri
     *            A <code>String</code> object containing the base URI that is added to your
     *            Service Bus namespace to form the URI to get an access token for the Service
     *            Bus service.
     * 
     *            To access the default public Azure service, pass "-sb.accesscontrol.windows.net/WRAPv0.9"
     * 
     * @return
     *         A <code>Configuration</code> object that can be used when creating an instance of the
     *         <code>ServiceBusService</code> class.
     * 
     */
    public static Configuration configureWithWrapAuthentication(String uri, String certificateLocation) {
        return configureWithWrapAuthentication(null, Configuration.getInstance(), uri, certificateLocation);
    }

    public static Configuration configureWithWrapAuthentication(String profile, Configuration configuration,
            String uri, String certificateLocation) {

        if (profile == null) {
            profile = "";
        }
        else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + URI, "https://" + uri);

        configuration.setProperty(profile + CERTIFICATE_LOCATION, certificateLocation);

        return configuration;
    }

}
