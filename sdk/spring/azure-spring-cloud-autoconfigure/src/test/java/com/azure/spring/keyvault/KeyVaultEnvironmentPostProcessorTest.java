// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyVaultEnvironmentPostProcessorTest {

    private MockEnvironment environment;
    private MutablePropertySources propertySources;
    private final Map<String, Object> keyVaultProperties = new HashMap<>();

    @BeforeEach
    void setup() {
        environment = new MockEnvironment();
        keyVaultProperties.clear();
        propertySources = environment.getPropertySources();
    }



    @Test
    void postProcessorHasConfiguredOrder() {
        final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor(new DeferredLog());
        assertEquals(processor.getOrder(), KeyVaultEnvironmentPostProcessor.ORDER);
    }

    // TODO (xiada): add tests


}

