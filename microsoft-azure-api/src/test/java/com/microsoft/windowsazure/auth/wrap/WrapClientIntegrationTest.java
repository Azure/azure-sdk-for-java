package com.microsoft.windowsazure.auth.wrap;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.auth.wrap.WrapClient;
import com.microsoft.windowsazure.common.Configuration;

public class WrapClientIntegrationTest {
    private Configuration createConfiguration() {
        Configuration config = new Configuration();
        config.setProperty("wrapClient.uri", "https://lodejard-sb.accesscontrol.windows.net/WRAPv0.9");
        config.setProperty("wrapClient.scope", "http://lodejard.servicebus.windows.net/");
        config.setProperty("wrapClient.name", "owner");
        config.setProperty("wrapClient.password", "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");
        return config;
    }

    @Test
    public void wrapClientWillAcquireAccessToken() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        WrapClient client = config.create(WrapClient.class);

        // Act
        String accessToken = client.getAccessToken();

        // Assert
        Assert.assertNotNull(accessToken);
    }
}
