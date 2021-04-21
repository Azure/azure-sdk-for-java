// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;

public class VisualStudioCacheAccessorTests {

    @Test
    public void testReadJsonFile() throws Exception {
        // setup
        VisualStudioCacheAccessor accessor = new VisualStudioCacheAccessor();
        JsonNode jsonRead = accessor.readJsonFile(getPath("settings.json"));
        Assert.assertEquals("first", jsonRead.get("editor.suggestSelection").asText());
        Assert.assertEquals("/Contents/Home", jsonRead.get("java.home").asText());
        Assert.assertEquals(12, jsonRead.size());
    }

    private String getPath(String filename) {
        String path =  getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
