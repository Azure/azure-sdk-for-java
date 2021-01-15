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
import java.util.List;

@RunWith(PowerMockRunner.class)
public class CertificateUtilTests {

    @Test(expected = CertificateExpiredException.class)
    public void testPublicKey() throws Exception {
        String pemPath = getPath("certificate.pem");
        byte[] pemCertificateBytes = Files.readAllBytes(Paths.get(pemPath));
        List<X509Certificate> x509CertificateList = CertificateUtil.publicKeyFromPem(pemCertificateBytes);
        x509CertificateList.get(0).checkValidity(Date.valueOf(LocalDate.of(2025, 12, 25)));
    }

    @Test(expected = CertificateExpiredException.class)
    public void testPublicKeyChain() throws Exception {
        String pemPath = getPath("cert-chain.pem");
        byte[] pemCertificateBytes = Files.readAllBytes(Paths.get(pemPath));
        List<X509Certificate> x509CertificateList = CertificateUtil.publicKeyFromPem(pemCertificateBytes);
        Assert.assertEquals(2, x509CertificateList.size());
        x509CertificateList.get(0).checkValidity(Date.valueOf(LocalDate.of(4025, 12, 25)));
    }


    @Test
    public void testPrivateKey() throws Exception {
        String pemPath = getPath("key.pem");
        byte[] pemCertificateBytes = Files.readAllBytes(Paths.get(pemPath));
        PrivateKey privateKey = CertificateUtil.privateKeyFromPem(pemCertificateBytes);
        Assert.assertEquals("RSA", privateKey.getAlgorithm());
    }

    private String getPath(String filename) {

        String path =  getClass().getClassLoader().getResource(filename).getPath();
        if (path.contains(":")) {
            path = path.substring(1);
        }
        return path;
    }
}
