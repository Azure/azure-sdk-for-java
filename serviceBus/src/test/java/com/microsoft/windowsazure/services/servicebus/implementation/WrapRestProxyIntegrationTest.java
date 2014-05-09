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
import com.microsoft.windowsazure.core.UserAgentFilter;
import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.sun.jersey.api.client.Client;

public class WrapRestProxyIntegrationTest {
    @Test
    public void serviceCanBeCalledToCreateAccessToken() throws Exception {
        // Arrange
        Configuration config = Configuration.getInstance();
        overrideWithEnv(config, ServiceBusConfiguration.URI);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_URI);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_NAME);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_PASSWORD);
        WrapContract contract = new WrapRestProxy(config.create(Client.class),
                new UserAgentFilter());

        // Act
        String serviceBusUri = (String) config
                .getProperty(ServiceBusConfiguration.URI);
        String uri = (String) config
                .getProperty(ServiceBusConfiguration.WRAP_URI);
        String name = (String) config
                .getProperty(ServiceBusConfiguration.WRAP_NAME);
        String password = (String) config
                .getProperty(ServiceBusConfiguration.WRAP_PASSWORD);
        String scope = new URI("http", new URI(serviceBusUri).getAuthority(),
                new URI(serviceBusUri).getPath(), null, null).toString();
        WrapAccessTokenResult result = contract.wrapAccessToken(uri, name,
                password, scope);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
    }

    private static void overrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }
}
