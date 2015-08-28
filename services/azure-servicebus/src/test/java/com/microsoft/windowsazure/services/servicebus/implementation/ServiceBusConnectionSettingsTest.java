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

package com.microsoft.windowsazure.services.servicebus.implementation;

import com.microsoft.windowsazure.Configuration;
import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;

public class ServiceBusConnectionSettingsTest {

    @Test
    public void settingsAreParsedFromConnectionString() throws Exception {
        String ns = "myNamespace";
        String issuer = "myissuer";
        String secret = "mysecret";

        String connectionString = getConnectionString(ns, issuer, secret);

        ServiceBusConnectionSettings settings = new ServiceBusConnectionSettings(
                connectionString, null, null, null, null, null, null);

        assertEquals(String.format("https://%1$s.servicebus.windows.net/", ns),
                settings.getUri());
        assertEquals(String.format(
                "https://%1$s-sb.accesscontrol.windows.net/WRAPv0.9", ns),
                settings.getWrapUri());
        assertEquals(issuer, settings.getWrapName());
        assertEquals(secret, settings.getWrapPassword());
    }

    private String getConnectionString(String ns, String issuer, String secret) {
        return String
                .format("Endpoint=sb://%1$s.servicebus.windows.net/;SharedSecretIssuer=%2$s;SharedSecretValue=%3$s",
                        ns, issuer, secret);
    }

    private String getConnectionString(String ns, String stsEndpoint,
            String issuer, String secret) {
        return String
                .format("Endpoint=sb://%1$s.servicebus.windows.net/;StsEndpoint=https://%1$s%4$s;SharedSecretIssuer=%2$s;SharedSecretValue=%3$s",
                        ns, issuer, secret, stsEndpoint);
    }

    @Test
    public void settingsAreUsedFromConnectionStringInConfig() throws Exception {
        Configuration config = Configuration.load();
        ServiceBusConfiguration.configureWithConnectionString(null, config,
                getConnectionString("myNamespace", "owner", "secret"));

        ServiceBusConnectionSettings settings = config
                .create(ServiceBusConnectionSettings.class);

        assertEquals("https://myNamespace.servicebus.windows.net/",
                settings.getUri());
        assertEquals(
                "https://myNamespace-sb.accesscontrol.windows.net/WRAPv0.9",
                settings.getWrapUri());
        assertEquals("owner", settings.getWrapName());
        assertEquals("secret", settings.getWrapPassword());
    }

    @Test
    public void settingsAreUsedFromIndividualSettingsInConfiguration()
            throws Exception {
        Configuration config = Configuration.load();

        ServiceBusConfiguration.configureWithWrapAuthentication(config,
                "myNamespace", "owner", "secret", ".servicebus.windows.net/",
                "-sb.accesscontrol.windows.net/WRAPv0.9");

        ServiceBusConnectionSettings settings = config
                .create(ServiceBusConnectionSettings.class);

        assertEquals("https://myNamespace.servicebus.windows.net/",
                settings.getUri());
        assertEquals(
                "https://myNamespace-sb.accesscontrol.windows.net/WRAPv0.9",
                settings.getWrapUri());
        assertEquals("owner", settings.getWrapName());
        assertEquals("secret", settings.getWrapPassword());
    }

    @Test
    public void settingsPreferConnectionStringIfBothPresentInConfiguration()
            throws Exception {
        Configuration config = Configuration.load();

        ServiceBusConfiguration.configureWithWrapAuthentication(config,
                "myIndividualNamespace", "individualowner", "individualsecret",
                ".servicebus.windows.net/",
                "-sb.accesscontrol.windows.net/WRAPv0.9");

        ServiceBusConfiguration.configureWithConnectionString(null, config,
                getConnectionString("myNamespaceCS", "ownerCS", "secretCS"));

        ServiceBusConnectionSettings settings = config
                .create(ServiceBusConnectionSettings.class);

        assertEquals("https://myNamespaceCS.servicebus.windows.net/",
                settings.getUri());
        assertEquals(
                "https://myNamespaceCS-sb.accesscontrol.windows.net/WRAPv0.9",
                settings.getWrapUri());
        assertEquals("ownerCS", settings.getWrapName());
        assertEquals("secretCS", settings.getWrapPassword());
    }

    @Test
    public void canSetStSEndPointInConnectionString() throws Exception {
        ServiceBusConnectionSettings settings = new ServiceBusConnectionSettings(
                getConnectionString("myNs", "-some.accesscontrol.net", "owner",
                        "secret"), null, null, null, null, null, null);

        assertEquals("https://myNs.servicebus.windows.net/", settings.getUri());
        assertEquals("https://myNs-some.accesscontrol.net/WRAPv0.9",
                settings.getWrapUri());
        assertEquals("owner", settings.getWrapName());
        assertEquals("secret", settings.getWrapPassword());
    }
}
