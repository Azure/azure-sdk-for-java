// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import static org.mockito.Mockito.mock;

import java.security.cert.Certificate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClasspathCertificatesTest {

    private final Certificate certificate = mock(Certificate.class);

    @Test
    public void testSetCertificateEntry() {
        ClasspathCertificates classpathCertificates = new ClasspathCertificates();
        classpathCertificates.setCertificateEntry("myalias", certificate);
        Assertions.assertTrue(classpathCertificates.getAliases().contains("myalias"));
        Assertions.assertEquals(classpathCertificates.getCertificates().get("myalias"), certificate);
    }

}
