package com.microsoft.windowsazure.services.serviceBus.implementation;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.common.Configuration;

public class WrapTokenManagerIntegrationTest {
    @Test
    public void wrapClientWillAcquireAccessToken() throws Exception {
        // Arrange
        Configuration config = Configuration.getInstance();
        WrapTokenManager client = config.create("serviceBus", WrapTokenManager.class);

        // Act
        String accessToken = client.getAccessToken();

        // Assert
        Assert.assertNotNull(accessToken);
    }
}
