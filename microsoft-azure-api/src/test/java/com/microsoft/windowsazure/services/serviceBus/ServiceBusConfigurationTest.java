package com.microsoft.windowsazure.services.serviceBus;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;

public class ServiceBusConfigurationTest {
    @Test
    public void ConfigureSetsExpectedProperties() {
        // Arrange
        Configuration config = new Configuration();

        // Act
        ServiceBusConfiguration.configure(config, "alpha", "beta", "gamma");

        // Assert
        assertEquals("https://alpha.servicebus.windows.net/",
                config.getProperty("serviceBus.uri"));
        assertEquals("https://alpha-sb.accesscontrol.windows.net/WRAPv0.9",
                config.getProperty("serviceBus.wrap.uri"));
        assertEquals("beta",
                config.getProperty("serviceBus.wrap.name"));
        assertEquals("gamma",
                config.getProperty("serviceBus.wrap.password"));
        assertEquals("http://alpha.servicebus.windows.net/",
                config.getProperty("serviceBus.wrap.scope"));
    }

    @Test
    public void UsingProfileAddsPrefix() {
        // Arrange
        Configuration config = new Configuration();

        // Act
        ServiceBusConfiguration.configure("backup", config, "alpha", "beta", "gamma");

        // Assert
        assertEquals("https://alpha.servicebus.windows.net/",
                config.getProperty("backup.serviceBus.uri"));
        assertEquals("https://alpha-sb.accesscontrol.windows.net/WRAPv0.9",
                config.getProperty("backup.serviceBus.wrap.uri"));
        assertEquals("beta",
                config.getProperty("backup.serviceBus.wrap.name"));
        assertEquals("gamma",
                config.getProperty("backup.serviceBus.wrap.password"));
        assertEquals("http://alpha.servicebus.windows.net/",
                config.getProperty("backup.serviceBus.wrap.scope"));
    }
}
