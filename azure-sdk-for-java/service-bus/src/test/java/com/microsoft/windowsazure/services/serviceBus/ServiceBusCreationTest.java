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
package com.microsoft.windowsazure.services.serviceBus;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.serviceBus.implementation.ServiceBusExceptionProcessor;

public class ServiceBusCreationTest {
    @Test
    public void theServiceClassMayBeCreatedDirectlyViaSingletonConfig() throws Exception {
        ServiceBusConfiguration.configureWithWrapAuthentication("my-namespace", "my-identity", "my-shared-secret");
        ServiceBusContract service = ServiceBusService.create();

        assertNotNull(service);
        assertEquals(ServiceBusExceptionProcessor.class, service.getClass());
    }

    public Configuration newConfiguration() {
        Configuration config = new Configuration();
        ServiceBusConfiguration.configureWithWrapAuthentication(config, "my-namespace", "my-identity", "my-shared-secret");
        return config;
    }

    @Test
    public void theServiceClassMayBeCreatedDirectlyWithConfig() throws Exception {
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
}
