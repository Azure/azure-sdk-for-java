/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config.auth;

import com.google.common.io.BaseEncoding;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal utility class to parse certificate
 */
class CertificateUtils {
    private static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    private static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
    private static final Pattern privateKeyPattern =
            Pattern.compile("(?s)-----BEGIN PRIVATE KEY-----.*-----END PRIVATE KEY-----");
    private static final Pattern certificatePattern =
            Pattern.compile("(?s)-----BEGIN CERTIFICATE-----.*-----END CERTIFICATE-----");

    private static final String GET_PRIVATE_KEY_FAILED = "Failed to generate private key from PEM file. " +
            "Please check the format and content of the provided PEM file.";
    private static final String GET_CERTIFICATE_FAILED = "Failed to generate certificate from PEM file. " +
            "Please check the format and content of the provided PEM file.";

    public static PrivateKey readPrivateKeyFromPem(String pem) {
        Matcher matcher = privateKeyPattern.matcher(pem);
        matcher.find();

        String base64 = matcher.group()
                .replace(BEGIN_PRIVATE_KEY, "")
                .replace(END_PRIVATE_KEY, "")
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "");
        byte[] key = BaseEncoding.base64().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);

        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(spec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException(GET_PRIVATE_KEY_FAILED, ex);
        }
    }

    public static X509Certificate readX509CertificateFromPem(String pem) {
        Matcher matcher = certificatePattern.matcher(pem);
        matcher.find();

        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream stream = new ByteArrayInputStream(matcher.group().getBytes());
            return (X509Certificate) factory.generateCertificate(stream);
        } catch (CertificateException ex) {
            throw new IllegalStateException(GET_CERTIFICATE_FAILED, ex);
        }
    }
}
