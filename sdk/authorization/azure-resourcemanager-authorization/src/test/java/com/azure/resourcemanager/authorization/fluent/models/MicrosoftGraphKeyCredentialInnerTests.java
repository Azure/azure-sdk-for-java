// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.fluent.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MicrosoftGraphKeyCredentialInnerTests {
    @Test
    public void serializesKeyAsStandardBase64() throws Exception {
        byte[] key = { (byte) 0xFB, (byte) 0xFF };
        MicrosoftGraphKeyCredentialInner credential = new MicrosoftGraphKeyCredentialInner().withKey(key);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            credential.toJson(writer);
        }

        String json = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"key\":\"+/8=\""));

        try (JsonReader reader = JsonProviders.createReader(json)) {
            assertArrayEquals(key, MicrosoftGraphKeyCredentialInner.fromJson(reader).key());
        }
    }
}
