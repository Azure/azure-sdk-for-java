package com.microsoft.azure.services.serviceBus;

import static org.junit.Assert.*;
import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.ServiceBusContract;
import com.microsoft.azure.services.serviceBus.implementation.ServiceBusRestProxy;

public class ServiceBusCreationTest {
    @Test
    public void theServiceClassMayBeCreatedDirectlyViaSingletonConfig() throws Exception {
        ServiceBusConfiguration.configure("my-namespace", "my-identity", "my-shared-secret");
        ServiceBusService service = new ServiceBusService();

        assertNotNull(service);
        assertEquals(ServiceBusService.class, service.getClass());
    }

    public Configuration newConfiguration() {
        Configuration config = new Configuration();
        ServiceBusConfiguration.configure(config, "my-namespace", "my-identity", "my-shared-secret");
        return config;
    }

    @Test
    public void theServiceClassMayBeCreatedDirectlyWithConfig() throws Exception {
        Configuration config = newConfiguration();
        ServiceBusContract service = new ServiceBusService(config);

        assertNotNull(service);
        assertEquals(ServiceBusService.class, service.getClass());
    }

    @Test
    public void theServiceClassMayAlsoBeCreatedFromConfig() throws Exception {
        Configuration config = newConfiguration();
        ServiceBusContract service = config.create(ServiceBusService.class);

        assertNotNull(service);
        assertEquals(ServiceBusService.class, service.getClass());
    }

    @Test
    public void testDefaultBuilderCreatesServiceImpl() throws Exception {
        Configuration config = newConfiguration();
        ServiceBusContract service = config.create(ServiceBusContract.class);

        assertNotNull(service);
    }
}
