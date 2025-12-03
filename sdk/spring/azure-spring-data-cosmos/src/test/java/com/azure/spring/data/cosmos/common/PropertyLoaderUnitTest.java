// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropertyLoaderUnitTest {

    @Test
    public void testGetProjectVersion() {
        final String version = PropertyLoader.getProjectVersion();

        Assertions.assertNotNull(version);
        Assertions.assertNotEquals(version, "");
        Assertions.assertNotEquals(version, "@project.version@");
    }
}
