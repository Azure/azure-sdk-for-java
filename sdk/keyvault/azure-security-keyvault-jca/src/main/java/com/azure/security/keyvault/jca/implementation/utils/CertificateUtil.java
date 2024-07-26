// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Util used to load certificate from pem file.
 */
public final class CertificateUtil {
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    public static Certificate[] loadCertificatesFromSecretBundleValue(String string)
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        if (string.contains(BEGIN_CERTIFICATE)) {
            return loadCertificatesFromSecretBundleValuePem(string);
        } else {
            return loadCertificatesFromSecretBundleValuePKCS12(string);
        }
    }

    public static Certificate[] loadCertificatesFromSecretBundleValuePem(InputStream inputStream)
        throws IOException, CertificateException {
        List<Certificate> certificates = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        while (reader.ready()) {
            String line = reader.readLine();
            if (line.contains(BEGIN_CERTIFICATE)) {
                builder = new StringBuilder();
            }
            builder.append(line).append('\n');
            if (line.contains(END_CERTIFICATE)) {
                InputStream stream = new ByteArrayInputStream(builder.toString().getBytes());
                Certificate certificate = factory.generateCertificate(stream);
                certificates.add(certificate);
            }
        }
        return certificates.toArray(new Certificate[0]);
    }

    public static Certificate[] loadCertificatesFromSecretBundleValuePem(String string) throws IOException, CertificateException {
        InputStream inputStream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        return loadCertificatesFromSecretBundleValuePem(inputStream);
    }

    public static Certificate[] loadCertificatesFromSecretBundleValuePKCS12(InputStream inputStream)
        throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(inputStream, "".toCharArray());
        String alias = keyStore.aliases().nextElement();
        return keyStore.getCertificateChain(alias);
    }

    public static Certificate[] loadCertificatesFromSecretBundleValuePKCS12(String string)
        throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(string));
        return loadCertificatesFromSecretBundleValuePKCS12(inputStream);
    }

    public static Certificate loadX509CertificateFromFile(InputStream inputStream) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificate(inputStream);
    }

    public static Certificate[] loadX509CertificatesFromFile(InputStream inputStream) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificates(inputStream).stream()
            .map(o -> (Certificate) o)
            .collect(Collectors.toList())
            .toArray(new Certificate[0]);
    }
}
