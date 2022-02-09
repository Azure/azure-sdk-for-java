// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.util.Base64Util;
import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for various operations for interacting with certificates.
 */
public final class CertificateUtil {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateUtil.class);

    /**
     * Extracts the PrivateKey from a PEM certificate.
     * @param pem the contents of a PEM certificate.
     * @return the PrivateKey
     */
    public static PrivateKey privateKeyFromPem(byte[] pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN PRIVATE KEY-----.*-----END PRIVATE KEY-----");
        Matcher matcher = pattern.matcher(new String(pem, StandardCharsets.UTF_8));
        if (!matcher.find()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Certificate file provided is not a valid PEM file."));
        }
        String base64 = matcher.group()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")
            .replace("\r", "");
        byte[] key = Base64Util.decode(base64.getBytes(StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Extracts the X509Certificate certificate/certificate-chain from a PEM certificate.
     * @param pem the contents of a PEM certificate.
     * @return the {@link List} of X509Certificate certificate
     */
    public static List<X509Certificate> publicKeyFromPem(byte[] pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----");
        Matcher matcher = pattern.matcher(new String(pem, StandardCharsets.UTF_8));

        List<X509Certificate> x509CertificateList = new ArrayList<>();
        while (matcher.find()) {
            try {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                InputStream stream = new ByteArrayInputStream(matcher.group().getBytes(StandardCharsets.UTF_8));
                x509CertificateList.add((X509Certificate) factory.generateCertificate(stream));
            } catch (CertificateException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        }

        if (x509CertificateList.size() == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "PEM certificate provided does not contain -----BEGIN CERTIFICATE-----END CERTIFICATE----- block"));
        }

        return x509CertificateList;
    }

    private CertificateUtil() { }
}
