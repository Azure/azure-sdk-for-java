// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

public class VisualStudioCacheAccessorTests {

    @Test
    public void testReadJsonFile() throws Exception {
        // setup

        try (MockedStatic<VisualStudioCacheAccessor> cacheAccessorMockedStatic = mockStatic(VisualStudioCacheAccessor.class)) {
            String path = getPath("settings.json");
            cacheAccessorMockedStatic.when(VisualStudioCacheAccessor::getSettingsPath).thenReturn(path);
            VisualStudioCacheAccessor cacheAccessor = new VisualStudioCacheAccessor();
            Map<String, String> result = cacheAccessor.getUserSettingsDetails();
            assertEquals("AzureCloudFromFile", result.get("cloud"));
            assertEquals("AzureTenantFromFile", result.get("tenant"));
        }
    }

    private String getPath(String filename) {
        String path =  getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
