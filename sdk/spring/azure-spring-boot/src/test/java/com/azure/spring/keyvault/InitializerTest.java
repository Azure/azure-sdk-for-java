// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import com.azure.spring.utils.Constants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application.properties")
public class InitializerTest {

    @Autowired
    ApplicationContext context;

    @Test
    public void testAzureKvPropertySourceNotInitialized() {
        final MutablePropertySources sources =
            ((ConfigurableEnvironment) context.getEnvironment()).getPropertySources();

        assertFalse(sources.contains(Constants.AZURE_KEYVAULT_PROPERTYSOURCE_NAME), "PropertySources should not "
            + "contains azurekv when enabled=false");
    }
}
