// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CertificateUtilTest {

    @Test
    public void loadCertificatesFromPemTest()
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        String pemFile = "src/test/resources/certificate/chain.pem";
        String pemString = new String(Files.readAllBytes(Paths.get(pemFile)), StandardCharsets.UTF_8);
        assertEquals(3, CertificateUtil.loadCertificates(pemString).length);
        String pfxFile = "src/test/resources/certificate/chain.pfx.txt";
        String pfxString = new String(Files.readAllBytes(Paths.get(pfxFile)), StandardCharsets.UTF_8);
        assertEquals(3, CertificateUtil.loadCertificates(pfxString).length);
    }
}
