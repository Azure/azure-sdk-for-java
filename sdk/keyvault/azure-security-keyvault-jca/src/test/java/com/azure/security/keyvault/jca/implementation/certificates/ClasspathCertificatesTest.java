// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.cert.Certificate;

public class ClasspathCertificatesTest {

    private final Certificate certificate = new MockCertificate(null);

    @Test
    public void testSetCertificateEntry() {
        ClasspathCertificates classpathCertificates = new ClasspathCertificates();
        classpathCertificates.setCertificateEntry("myalias", certificate);
        Assertions.assertTrue(classpathCertificates.getAliases().contains("myalias"));
        Assertions.assertEquals(classpathCertificates.getCertificates().get("myalias"), certificate);
    }

}
