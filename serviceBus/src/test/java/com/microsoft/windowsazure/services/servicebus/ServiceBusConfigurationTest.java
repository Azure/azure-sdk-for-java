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

import com.microsoft.windowsazure.Configuration;
import static org.junit.Assert.*;

import org.junit.Test;

public class ServiceBusConfigurationTest {
    @Test
    public void ConfigureSetsExpectedProperties() {
        // Arrange
        Configuration config = new Configuration();

        // Act
        ServiceBusConfiguration.configureWithWrapAuthentication(config,
                "alpha", "beta", "gamma", ".servicebus.windows.net/",
                "-sb.accesscontrol.windows.net/WRAPv0.9");

        // Assert
        assertEquals("https://alpha.servicebus.windows.net/",
                config.getProperty("serviceBus.uri"));
        assertEquals("https://alpha-sb.accesscontrol.windows.net/WRAPv0.9",
                config.getProperty("serviceBus.wrap.uri"));
        assertEquals("beta", config.getProperty("serviceBus.wrap.name"));
        assertEquals("gamma", config.getProperty("serviceBus.wrap.password"));
    }

    @Test
    public void UsingProfileAddsPrefix() {
        // Arrange
        Configuration config = new Configuration();

        // Act
        ServiceBusConfiguration.configureWithWrapAuthentication("backup",
                config, "alpha", "beta", "gamma", ".servicebus.windows.net/",
                "-sb.accesscontrol.windows.net/WRAPv0.9");

        // Assert
        assertEquals("https://alpha.servicebus.windows.net/",
                config.getProperty("backup.serviceBus.uri"));
        assertEquals("https://alpha-sb.accesscontrol.windows.net/WRAPv0.9",
                config.getProperty("backup.serviceBus.wrap.uri"));
        assertEquals("beta", config.getProperty("backup.serviceBus.wrap.name"));
        assertEquals("gamma",
                config.getProperty("backup.serviceBus.wrap.password"));
    }

    @Test
    public void configureWithSASSetsExpectedProperties() {
        Configuration config = new Configuration();

        ServiceBusConfiguration.configureWithSASAuthentication(config, "alpha",
                "beta", "gamma", ".servicebus.windows.net/");

        assertEquals("https://alpha.servicebus.windows.net/", config.getProperty("serviceBus.uri"));
        assertEquals("beta", config.getProperty("serviceBus.sas.keyname"));
        assertEquals("gamma", config.getProperty("serviceBus.sas.key"));
    }

    @Test
    public void configureWithSASAndProfileAddsPrefix() {
        Configuration config = new Configuration();

        ServiceBusConfiguration.configureWithSASAuthentication("prefix", config, "alpha",
                "beta", "gamma", ".servicebus.windows.net/");

        assertEquals("https://alpha.servicebus.windows.net/", config.getProperty("prefix.serviceBus.uri"));
        assertEquals("beta", config.getProperty("prefix.serviceBus.sas.keyname"));
        assertEquals("gamma", config.getProperty("prefix.serviceBus.sas.key"));
    }
}
