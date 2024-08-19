// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

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
    /**
     * Test the {@link JsonConverterUtil#fromJson(Class, String)} method.
     */
    @Test
    public void testFromJson() throws IOException {
        String string = "{ \"cer\": \"cer\" }";
        CertificateBundle bundle = JsonConverterUtil.fromJson(CertificateBundle.class, string);

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
}
