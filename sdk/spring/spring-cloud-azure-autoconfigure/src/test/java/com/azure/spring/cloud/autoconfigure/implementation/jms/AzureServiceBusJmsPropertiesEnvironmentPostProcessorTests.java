// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzureServiceBusJmsPropertiesEnvironmentPostProcessorTests {

    private final AzureServiceBusJmsPropertiesEnvironmentPostProcessor processor =
        new AzureServiceBusJmsPropertiesEnvironmentPostProcessor();
    private final MockEnvironment environment = new MockEnvironment();

    @Test
    void testDefaultPool() {
        processor.postProcessEnvironment(environment, null);
        assertEquals("true", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }

}
