// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnsupportedJacksonVersionsTests {
    @Test
    public void testme() {
        String jacksonVersionString = ObjectMapper.class.getPackage().getImplementationVersion();
        String azureCoreVersion = CoreUtils
            .getProperties("azure-core.properties")
            .getOrDefault("version", null);

        JacksonVersion version = JacksonVersion.getInstance();
        String helpInfo = version.getHelpInfo();
        assertTrue(helpInfo.contains("jackson-annotations=" + jacksonVersionString));
        assertTrue(helpInfo.contains("jackson-core=" + jacksonVersionString));
        assertTrue(helpInfo.contains("jackson-databind=" + jacksonVersionString));
        assertTrue(helpInfo.contains("jackson-dataformat-xml=" + jacksonVersionString));
        assertTrue(helpInfo.contains("jackson-datatype-jsr310=" + jacksonVersionString));
        assertTrue(helpInfo.contains("azure-core=" + azureCoreVersion));

        Error error = assertThrows(Error.class, () -> new JacksonAdapter());
        assertTrue(error.getMessage().contains(helpInfo));
    }
}
