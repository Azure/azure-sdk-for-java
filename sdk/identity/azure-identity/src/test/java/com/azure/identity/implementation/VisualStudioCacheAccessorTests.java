// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VisualStudioCacheAccessorTests {

    @Test
    public void testReadJsonFile() throws Exception {
        // setup
        JsonNode jsonRead = VisualStudioCacheAccessor.readJsonFile(getPath("settings.json"));
        assertEquals("first", jsonRead.get("editor.suggestSelection").asText());
        assertEquals("/Contents/Home", jsonRead.get("java.home").asText());
        assertEquals(12, jsonRead.size());
    }

    private String getPath(String filename) {
        String path =  getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
