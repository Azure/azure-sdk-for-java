// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AzureSpringIdentifierTests {

    @Test
    void testFormatVersion() {
        Assertions.assertEquals(AzureSpringIdentifier.formatVersion("4.10.0"), "4.10.0");
        Assertions.assertEquals(AzureSpringIdentifier.formatVersion("4.9.0-beta.1"), "4.9.0-beta.1");
        Assertions.assertEquals(AzureSpringIdentifier.formatVersion("4.10.0-beta.1"), "4.10.0-b.1");
        Assertions.assertEquals(AzureSpringIdentifier.formatVersion("4.10.0-alpha.1"), "4.10.0-a.1");
        Assertions.assertThrows(RuntimeException.class, () -> AzureSpringIdentifier.formatVersion("4.10.0-SNAPSHOT"));
    }

}
