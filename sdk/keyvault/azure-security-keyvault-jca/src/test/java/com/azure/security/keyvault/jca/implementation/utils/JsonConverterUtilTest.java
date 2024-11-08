// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.ReadValueCallback;
import com.azure.json.implementation.jackson.core.JsonParseException;
import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.model.CertificateBundle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JUnit tests for the {@link JsonConverterUtil} class.
 */
public class JsonConverterUtilTest {

    /**
     * Test the {@link JsonConverterUtil#fromJson(ReadValueCallback, String)} method.
     */
    @Test
    public void testFromJson() throws IOException {
        String string = "{ \"cer\": \"cer\" }";
        CertificateBundle bundle = JsonConverterUtil.fromJson(CertificateBundle::fromJson, string);

        assertNotNull(bundle);
        assertEquals("cer", bundle.getCer());
    }

    /**
     * Test toJson method.
     */
    @Test
    public void testToJson() {
        CertificateBundle bundle = new CertificateBundle();

        bundle.setCer("value");

        String string = JsonConverterUtil.toJson(bundle);

        assertTrue(string.contains("cer"));
        assertTrue(string.contains("\"value\""));
    }

    @Test
    void testFromJsonWithInvalidTokenResponseBody() {
        final String accessTokenBody = getAccessTokenBody("src/test/resources/aad/invalid-access-token-response.json");
        assertThrowsExactly(JsonParseException.class, () -> {
            try (JsonReader reader = JsonProviders.createReader(accessTokenBody)) {
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("expires_in".equals(fieldName)) {
                        reader.getLong();
                    } else {
                        reader.skipChildren();
                    }
                }
            }
        });
    }

    @Test
    void testFromJsonWithTokenResponseBody() {
        String accessTokenBody = getAccessTokenBody("src/test/resources/aad/access-token-response.json");
        AccessToken accessToken = null;
        try {
            accessToken = JsonConverterUtil.fromJson(AccessToken::fromJson, accessTokenBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(accessToken);
        assertEquals("test_access_token_value", accessToken.getAccessToken());
    }

    private static String getAccessTokenBody(String filePath) {
        String accessTokenBody = null;
        try {
            accessTokenBody = new String(Files.readAllBytes(
                Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
        return accessTokenBody;
    }
}
