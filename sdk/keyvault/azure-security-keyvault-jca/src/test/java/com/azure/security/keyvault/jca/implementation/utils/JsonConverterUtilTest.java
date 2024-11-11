// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.json.ReadValueCallback;
import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.model.CertificateBundle;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JUnit tests for the {@link JsonConverterUtil} class.
 */
public class JsonConverterUtilTest {

    static final String DUMMY_TOKEN_RESPONSE_BODY = "{\"token_type\":\"Bearer\",\"expires_in\":\"3599\","
        + "\"ext_expires_in\":\"3599\",\"expires_on\":\"1731052824\",\"not_before\":\"1731048924\","
        + "\"resource\":\"https://vault.azure.net\",\"access_token\":\"test_access_token_value\"}";

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
    void testFromJsonWithTokenResponseBody() {
        AccessToken accessToken = null;
        try {
            accessToken = JsonConverterUtil.fromJson(AccessToken::fromJson, DUMMY_TOKEN_RESPONSE_BODY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(accessToken);
        assertEquals("test_access_token_value", accessToken.getAccessToken());
    }
}
