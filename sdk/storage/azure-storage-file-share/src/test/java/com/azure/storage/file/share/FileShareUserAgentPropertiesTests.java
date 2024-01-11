// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileShareUserAgentPropertiesTests {
    @Test
    void userAgentPropertiesNotNull() {
        Map<String, String> properties = CoreUtils.getProperties("azure-storage-file-share.properties");
        assertNotNull(properties);
        assertEquals("azure-storage-file-share", properties.get("name"));
        String version = properties.get("version");
        assertNotNull(version);
        assertTrue(version.matches("(\\d)+.(\\d)+.(\\d)+([-a-zA-Z0-9.])*"));
    }
}
