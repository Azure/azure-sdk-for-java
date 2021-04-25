// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.http;

import com.azure.jca.http.model.CertificateBundle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JUnit tests for the JsonbJsonConverter class.
 */
public class JacksonJsonConverterTest {

    /**
     * Test fromJson method.
     */
    @Test
    public void testFromJson() {
        String string = "{ \"cer\": \"cer\" }";
        JacksonJsonConverter converter = new JacksonJsonConverter();
        CertificateBundle bundle = (CertificateBundle) converter.fromJson(string, CertificateBundle.class);
        assertNotNull(bundle);
        assertEquals("cer", bundle.getCer());
    }

    /**
     * Test toJson method.
     *
     */
    @Test
    public void testToJson() {
        JacksonJsonConverter converter = new JacksonJsonConverter();
        CertificateBundle bundle = new CertificateBundle();
        bundle.setCer("value");
        String string = converter.toJson(bundle);
        assertTrue(string.contains("cer"));
        assertTrue(string.contains("\"value\""));
    }
}
