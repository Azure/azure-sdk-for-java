// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobCryptographyUserAgentPropertiesTests {
    @Test
    public void userAgentPropertiesNotNull() {
        Map<String, String> properties = CoreUtils.getProperties("azure-storage-blob-cryptography.properties");

        assertEquals("azure-storage-blob-cryptography", properties.get("name"));
        assertTrue(properties.get("version").matches("(\\d)+.(\\d)+.(\\d)+([-a-zA-Z0-9.])*"));
    }
}
