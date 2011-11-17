package com.microsoft.windowsazure.services.serviceBus.implementation;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.configuration.Configuration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;
import com.sun.jersey.api.client.Client;

public class WrapRestProxyIntegrationTest {
    @Test
    public void serviceCanBeCalledToCreateAccessToken() throws Exception {
        // Arrange
        Configuration config = Configuration.getInstance();
        WrapContract contract = new WrapRestProxy(config.create(Client.class));

        // Act
        String uri = (String) config.getProperty(ServiceBusConfiguration.WRAP_URI);
        String name = (String) config.getProperty(ServiceBusConfiguration.WRAP_NAME);
        String password = (String) config.getProperty(ServiceBusConfiguration.WRAP_PASSWORD);
        String scope = (String) config.getProperty(ServiceBusConfiguration.WRAP_SCOPE);
        WrapAccessTokenResult result = contract.wrapAccessToken(uri, name, password, scope);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
    }
}
