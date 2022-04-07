// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.core;

import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AzureSpringIdentifierTests {

    @Test
    public void testPomVersion() {
        Assertions.assertNotNull(AzureSpringIdentifier.VERSION);
        Assertions.assertNotEquals("unknown", AzureSpringIdentifier.VERSION);
    }
}
