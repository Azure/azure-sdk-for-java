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

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.implementation.ServiceBusExceptionProcessor;

public class ServiceBusCreationTest {
    @Test
    public void theServiceClassMayBeCreatedDirectlyViaSingletonConfig()
            throws Exception {
        ServiceBusConfiguration.configureWithWrapAuthentication("my-namespace",
                "my-identity", "my-shared-secret", ".servicebus.windows.net",
                "-sb.accesscontrol.windows.net/WRAPv0.9");
        ServiceBusContract service = ServiceBusService.create();

        assertNotNull(service);
        assertEquals(ServiceBusExceptionProcessor.class, service.getClass());
    }

    @Test
    public void theServiceClassMayBeCreatedWithSasDirectlyViaSingletonConfig()
            throws Exception {
        ServiceBusConfiguration.configureWithSASAuthentication("my-namespace",
                "my-key-name", "my-secret-key", ".servicebus.windows.net");
        ServiceBusContract service = ServiceBusService.create();

        assertNotNull(service);
        assertEquals(ServiceBusExceptionProcessor.class, service.getClass());
    }


    public Configuration newConfiguration() {
        Configuration config = new Configuration();
        ServiceBusConfiguration.configureWithWrapAuthentication(config,
                "my-namespace", "my-identity", "my-shared-secret",
                ".servicebus.windows.net",
                "-sb.accesscontrol.windows.net/WRAPv0.9");
        return config;
    }

    private Configuration newConfigurationWithProfile() {
        Configuration config = newConfiguration();
        ServiceBusConfiguration.configureWithWrapAuthentication("other",
                config, "my-other-namespace", "my-other-identity",
                "my-shared-secret", ".servicebus.windows.net",
                "-sb.accesscontrol.windows.net/WRAPv0.9");
        return config;
    }

    private Configuration newConfigurationWithConnectionString() {
        Configuration config = newConfiguration();
        ServiceBusConfiguration.configureWithConnectionString(null, config,
                "Endpoint=https://my-other-namespace.servicebus.windows.net/;"
                        + "SharedSecretIssuer=owner;"
                        + "SharedSecretValue=my-shared-secret");
        return config;
    }

    @Test
    public void theServiceClassMayBeCreatedDirectlyWithConfig()
            throws Exception {
        Configuration config = newConfiguration();
        ServiceBusContract service = ServiceBusService.create(config);

        assertNotNull(service);
        assertEquals(ServiceBusExceptionProcessor.class, service.getClass());
    }

    @Test
    public void theServiceClassMayAlsoBeCreatedFromConfig() throws Exception {
        Configuration config = newConfiguration();
        ServiceBusContract service = config.create(ServiceBusContract.class);

        assertNotNull(service);
        assertEquals(ServiceBusExceptionProcessor.class, service.getClass());
    }

    @Test
    public void testDefaultBuilderCreatesServiceImpl() throws Exception {
        Configuration config = newConfiguration();
        ServiceBusContract service = config.create(ServiceBusContract.class);

        assertNotNull(service);
    }

    @Test
    public void theServiceClassCanBeCreatedThroughAProfile() throws Exception {
        Configuration config = newConfigurationWithProfile();
        ServiceBusContract service = config.create("other",
                ServiceBusContract.class);

        assertNotNull(service);
        assertEquals(ServiceBusExceptionProcessor.class, service.getClass());
    }

    @Test
    public void theServiceClassCanBeCreatedThroughConnectionString()
            throws Exception {
        Configuration config = newConfigurationWithConnectionString();

        ServiceBusContract service = config.create(ServiceBusContract.class);
        assertNotNull(service);
    }
}
