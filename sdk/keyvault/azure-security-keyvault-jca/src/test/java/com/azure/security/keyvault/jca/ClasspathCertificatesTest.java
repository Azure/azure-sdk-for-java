// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.security.cert.Certificate;

import static org.mockito.Mockito.mock;

public class ClasspathCertificatesTest {

    private Key key = mock(Key.class);

    private Certificate certificate = mock(Certificate.class);

    private ClasspathCertificates classpathCertificates;

    @Test
    public void testSetCertificateEntry() {
        classpathCertificates = new ClasspathCertificates();
        classpathCertificates.setCertificateEntry("myalias", certificate);
        Assertions.assertTrue(classpathCertificates.getAliases().contains("myalias"));
        Assertions.assertEquals(classpathCertificates.getCertificates().get("myalias"), certificate);
    }

}
