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
        WrapTokenManager client = config.create("serviceBus", WrapTokenManager.class);

        // Act
        String wrapScope = (String) config.getProperty(ServiceBusConfiguration.WRAP_SCOPE);
        String accessToken = client.getAccessToken(new URI(wrapScope));

        // Assert
        Assert.assertNotNull(accessToken);
    }
}
