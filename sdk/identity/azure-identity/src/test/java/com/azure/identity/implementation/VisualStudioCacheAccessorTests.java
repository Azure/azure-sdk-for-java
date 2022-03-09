// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Assert;
import org.junit.Test;

public class VisualStudioCacheAccessorTests {

    @Test
    public void testReadJsonFile() throws Exception {
        // setup
        JsonParser parser = VisualStudioCacheAccessor.readJsonFile(getPath("settings.json"));
        int fieldCount = 0;
        String editorSuggestSelection = null;
        String javaHome = null;

        while (parser.nextToken() != null) {
            if (parser.currentToken() == JsonToken.FIELD_NAME) {
                fieldCount++;
                if ("editor.suggestSelection".equals(parser.currentName())) {
                    editorSuggestSelection = parser.nextTextValue();
                } else if ("java.home".equals(parser.currentName())) {
                    javaHome = parser.nextTextValue();
                }
            }
        }

        Assert.assertEquals("first", editorSuggestSelection);
        Assert.assertEquals("/Contents/Home", javaHome);
        Assert.assertEquals(12, fieldCount);
    }

    private String getPath(String filename) {
        String path =  getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
