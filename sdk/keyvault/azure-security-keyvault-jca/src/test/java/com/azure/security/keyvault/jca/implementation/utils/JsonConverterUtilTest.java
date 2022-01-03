// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.security.keyvault.jca.implementation.model.CertificateBundle;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests for the JsonbJsonConverter class.
 */
public class JsonConverterUtilTest {

    /**
     * Test fromJson method.
     */
    @Test
    public void testFromJson() {
        String string = "{ \"cer\": \"cer\" }";
        CertificateBundle bundle = (CertificateBundle) JsonConverterUtil.fromJson(string, CertificateBundle.class);
        assertNotNull(bundle);
        assertEquals("cer", bundle.getCer());
    }

    /**
     * Test toJson method.
     *
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
