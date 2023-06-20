// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class VisualStudioCacheAccessorTests {

    @Test
    public void testReadJsonFile() throws Exception {
        // setup
        Map<String, Object> jsonRead = VisualStudioCacheAccessor.readJsonFile(getPath("settings.json"));
        Assert.assertEquals("first", String.valueOf(jsonRead.get("editor.suggestSelection")));
        Assert.assertEquals("/Contents/Home", String.valueOf(jsonRead.get("java.home")));
        Assert.assertEquals(12, jsonRead.size());
    }

    private String getPath(String filename) {
        String path = getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
