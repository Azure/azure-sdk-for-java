// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.rest.CertificateBundle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JUnit tests for the JsonbJsonConverter class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
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
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testToJson() throws Exception {
        JacksonJsonConverter converter = new JacksonJsonConverter();
        CertificateBundle bundle = new CertificateBundle();
        bundle.setCer("value");
        String string = converter.toJson(bundle);
        assertTrue(string.contains("cer"));
        assertTrue(string.contains("\"value\""));
    }
}
