// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.implementation.certificates.JreCertificates;
import org.junit.jupiter.api.Test;
import java.security.cert.Certificate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class JreKeyStoreTest {

    @Test
    public void testJreKsEntries() {
        JreCertificates jreCertificates = JreCertificates.getInstance();
        assertNotNull(jreCertificates);
        assertNotNull(jreCertificates.getAliases());
        Map<String, Certificate> certs = jreCertificates.getCertificates();
        assertTrue(certs.size() > 0);
        assertNotNull(jreCertificates.getCertificateKeys());
    }


}
