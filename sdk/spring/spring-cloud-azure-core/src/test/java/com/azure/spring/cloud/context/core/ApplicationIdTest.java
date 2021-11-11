// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core;

import com.azure.spring.core.AzureSpringIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ApplicationIdTest {

    @Test
    public void testPomVersion() {
        Assertions.assertNotNull(AzureSpringIdentifier.VERSION);
        Assertions.assertNotEquals("unknown", AzureSpringIdentifier.VERSION);
    }
}
