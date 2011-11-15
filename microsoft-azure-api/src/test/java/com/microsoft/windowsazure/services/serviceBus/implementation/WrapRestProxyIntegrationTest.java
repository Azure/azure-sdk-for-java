package com.microsoft.windowsazure.services.serviceBus.implementation;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.configuration.Configuration;
import com.sun.jersey.api.client.Client;

public class WrapRestProxyIntegrationTest {
    @Test
    public void serviceCanBeCalledToCreateAccessToken() throws Exception {
        // Arrange
        Configuration config = new Configuration();
        WrapContract contract = new WrapRestProxy(config.create(Client.class));

        // Act
        WrapAccessTokenResult result = contract.wrapAccessToken("https://lodejard-sb.accesscontrol.windows.net/WRAPv0.9", "owner",
                "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=", "http://lodejard.servicebus.windows.net");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
    }
}
