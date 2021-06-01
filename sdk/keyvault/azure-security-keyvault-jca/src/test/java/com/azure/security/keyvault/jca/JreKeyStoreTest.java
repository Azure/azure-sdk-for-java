// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.security.Security;
import java.security.cert.Certificate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JreKeyStoreTest {
    @BeforeAll
    public static void init() {
        /*
         * Add JCA provider.
         */
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);



    }

    @Test
    public void testJreKsEntries() {
        JreCertificates jreCertificates = JreCertificates.getInstance();
        assertNotNull(jreCertificates);
        assertNotNull(jreCertificates.getAliases());
        Map<String, Certificate> certs = jreCertificates.getCertificates();
        assertTrue(certs.containsKey("globalsignr2ca [jdk]"));
        assertNotNull(certs.get("globalsignr2ca [jdk]"));
        assertNotNull(jreCertificates.getCertificateKeys());
    }


}
