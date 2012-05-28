/**
 * Copyright 2011 Microsoft Corporation
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

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;

public class WrapTokenManagerIntegrationTest {
    @Test
    public void wrapClientWillAcquireAccessToken() throws Exception {
        // Arrange
        Configuration config = Configuration.getInstance();
        overrideWithEnv(config, ServiceBusConfiguration.URI);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_URI);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_NAME);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_PASSWORD);
        WrapTokenManager client = config.create("serviceBus", WrapTokenManager.class);

        // Act
        URI serviceBusURI = new URI((String) config.getProperty(ServiceBusConfiguration.URI));
        String accessToken = client.getAccessToken(serviceBusURI);

        // Assert
        Assert.assertNotNull(accessToken);
    }

    private static void overrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }
}
