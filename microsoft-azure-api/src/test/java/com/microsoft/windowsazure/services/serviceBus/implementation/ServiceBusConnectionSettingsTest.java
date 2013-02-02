/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.microsoft.windowsazure.services.serviceBus.implementation;

import org.junit.Test;

import static junit.framework.Assert.*;

public class ServiceBusConnectionSettingsTest {

    @Test
    public void settingsAreParsedFromConnectionString() throws Exception {
        String ns = "myNamespace";
        String issuer = "myissuer";
        String secret = "mysecret";

        String connectionString = String.format(
                "Endpoint=sb://%1$s.servicebus.windows.net/;SharedSecretIssuer=%2$s;SharedSecretValue=%3$s",
                ns, issuer, secret);

        ServiceBusConnectionSettings settings = new ServiceBusConnectionSettings(connectionString, null, null, null, null);

        assertEquals(String.format("https://%1$s.servicebus.windows.net/", ns), settings.getUri());
        assertEquals(String.format("https://%1$s-sb.accesscontrol.windows.net/WRAPv0.9", ns), settings.getWrapUri());
        assertEquals(issuer, settings.getWrapName());
        assertEquals(secret, settings.getWrapPassword());
    }
}
