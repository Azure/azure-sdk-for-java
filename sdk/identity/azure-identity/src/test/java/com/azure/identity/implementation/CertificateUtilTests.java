// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.identity.implementation.util.CertificateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.time.LocalDate;

@RunWith(PowerMockRunner.class)
public class CertificateUtilTests {

    @Test(expected = CertificateExpiredException.class)
    public void testPublicKey() throws Exception {
        String pemPath = getClass().getClassLoader().getResource("certificate.pem").getPath();
        byte[] pemCertificateBytes = Files.readAllBytes(Paths.get(pemPath));
        X509Certificate x509Certificate = CertificateUtil.publicKeyFromPem(pemCertificateBytes);
        x509Certificate.checkValidity(Date.valueOf(LocalDate.of(2025, 12, 25)));
    }

    @Test
    public void testPrivateKey() throws Exception {
        String pemPath = getClass().getClassLoader().getResource("key.pem").getPath();
        byte[] pemCertificateBytes = Files.readAllBytes(Paths.get(pemPath));
        PrivateKey privateKey = CertificateUtil.privateKeyFromPem(pemCertificateBytes);
        Assert.assertEquals("RSA", privateKey.getAlgorithm());
    }
}
