// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import org.junit.Assert;
import org.junit.Test;

public class PropertyLoaderUnitTest {

    @Test
    public void testGetProjectVersion() {
        final String version = PropertyLoader.getProjectVersion();

        Assert.assertNotNull(version);
        Assert.assertNotEquals(version, "");
    }
}
