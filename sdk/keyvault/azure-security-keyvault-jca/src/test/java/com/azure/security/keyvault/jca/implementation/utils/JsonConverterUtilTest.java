// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.json.ReadValueCallback;
import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.model.CertificateBundle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    public void testFromJson() {
        CertificateBundle bundle
            = assertDoesNotThrow(() -> JsonConverterUtil.fromJson(CertificateBundle::fromJson, "{\"cer\":\"cer\"}"));

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

        assertTrue(string.contains("\"cer\""));
        assertTrue(string.contains("\"value\""));
    }

    @Test
    void testFromJsonWithTokenResponseBody() {
        AccessToken accessToken
            = assertDoesNotThrow(() -> JsonConverterUtil.fromJson(AccessToken::fromJson, DUMMY_TOKEN_RESPONSE_BODY));
        assertNotNull(accessToken);
        assertEquals("test_access_token_value", accessToken.getAccessToken());
    }

    @Test
    void fromJsonDoesNotLogThePayload() throws IOException {
        List<String> loggedValues = new ArrayList<>();
        Handler collector = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                if (logRecord.getParameters() != null) {
                    for (Object parameter : logRecord.getParameters()) {
                        loggedValues.add(String.valueOf(parameter));
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        Logger logger = Logger.getLogger(JsonConverterUtil.class.getName());
        Level originalLevel = logger.getLevel();
        boolean originalUseParentHandlers = logger.getUseParentHandlers();

        logger.addHandler(collector);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        try {
            JsonConverterUtil.fromJson(AccessToken::fromJson, DUMMY_TOKEN_RESPONSE_BODY);
        } finally {
            logger.removeHandler(collector);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalUseParentHandlers);
        }

        assertTrue(loggedValues.stream().noneMatch(value -> value.contains("test_access_token_value")),
            "The payload must never be logged: it carries access tokens and PKCS12 key bundles");
    }
}
