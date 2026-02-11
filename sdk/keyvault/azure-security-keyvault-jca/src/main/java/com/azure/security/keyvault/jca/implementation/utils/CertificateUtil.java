// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.PKCS12SafeBag;
import org.bouncycastle.pkcs.PKCS12SafeBagFactory;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CertificateUtil {
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    public static Certificate[] loadCertificatesFromSecretBundleValue(String string) throws CertificateException,
        IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, PKCSException {
        Certificate[] certificates;
        if (string.contains(BEGIN_CERTIFICATE)) {
            certificates = loadCertificatesFromSecretBundleValuePem(string);
        } else {
            certificates = loadCertificatesFromSecretBundleValuePKCS12(string);
        }

        // Ensure certificates are in the correct order: end-entity (leaf) → intermediate(s) → root CA
        // This is required for jarsigner and other Java security tools
        return orderCertificateChain(certificates);
    }

    private static Certificate[] loadCertificatesFromSecretBundleValuePem(InputStream inputStream)
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

    private static Certificate[] loadCertificatesFromSecretBundleValuePem(String string)
        throws IOException, CertificateException {
        InputStream inputStream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        return loadCertificatesFromSecretBundleValuePem(inputStream);
    }

    private static Certificate[] loadCertificatesFromSecretBundleValuePKCS12(String string)
        throws IOException, CertificateException, PKCSException {
        List<Certificate> certificates = new ArrayList<>();
        PKCS12PfxPdu pfx = new PKCS12PfxPdu(Base64.getDecoder().decode(string.getBytes()));
        for (ContentInfo contentInfo : pfx.getContentInfos()) {
            if (contentInfo.getContentType().equals(PKCSObjectIdentifiers.encryptedData)) {
                PKCS12SafeBagFactory safeBagFactory = new PKCS12SafeBagFactory(contentInfo,
                    new JcePKCSPBEInputDecryptorProviderBuilder().build("\0".toCharArray()));
                PKCS12SafeBag[] safeBags = safeBagFactory.getSafeBags();
                for (PKCS12SafeBag safeBag : safeBags) {
                    Object bagValue = safeBag.getBagValue();
                    if (bagValue instanceof X509CertificateHolder) {
                        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                        InputStream in = new ByteArrayInputStream(((X509CertificateHolder) bagValue).getEncoded());
                        Certificate certificate = certFactory.generateCertificate(in);
                        certificates.add(certificate);
                    }
                }
            }
        }
        return certificates.toArray(new Certificate[0]);
    }

    public static Certificate loadX509CertificateFromFile(InputStream inputStream) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificate(inputStream);
    }

    public static Certificate[] loadX509CertificatesFromFile(InputStream inputStream) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificates(inputStream)
            .stream()
            .map(o -> (Certificate) o)
            .collect(Collectors.toList())
            .toArray(new Certificate[0]);
    }

    public static String getCertificateNameFromCertificateItemId(String id) {
        // Example id: https://mycertificates.vault.azure.net/certificates/mycert
        // Here, vault name is mycertificates.
        // Vault name must be a 3-24 character string, containing only 0-9, a-z, A-Z, and not consecutive -.
        String keyWord = "/certificates/";
        return id.substring(id.indexOf(keyWord) + keyWord.length());
    }

    /**
     * Orders a certificate chain to ensure it's in the correct order for jarsigner and Java security tools.
     * The correct order is: end-entity (leaf) certificate, intermediate CA(s), root CA.
     *
     * This method identifies the end-entity certificate (the one not issuing any other certificate in the chain)
     * and builds the chain from leaf to root by following the issuer relationships.
     *
     * @param certificates The array of certificates to order
     * @return The ordered array of certificates, or the original array if ordering cannot be determined
     */
    static Certificate[] orderCertificateChain(Certificate[] certificates) {
        if (certificates == null || certificates.length <= 1) {
            return certificates;
        }

        try {
            // Convert to X509Certificate for easier manipulation
            X509Certificate[] x509Certs = new X509Certificate[certificates.length];
            for (int i = 0; i < certificates.length; i++) {
                if (!(certificates[i] instanceof X509Certificate)) {
                    // If not X509, return original order
                    return certificates;
                }
                x509Certs[i] = (X509Certificate) certificates[i];
            }

            // Create a map of subject DN to certificate for quick lookup
            Map<String, X509Certificate> subjectToCert = new HashMap<>();
            for (X509Certificate cert : x509Certs) {
                subjectToCert.put(cert.getSubjectX500Principal().getName(), cert);
            }

            // Find the end-entity (leaf) certificate
            // It's the one that is not the issuer of any other certificate in the chain
            X509Certificate leafCert = null;
            for (X509Certificate cert : x509Certs) {
                boolean isIssuerOfOther = false;
                String certSubject = cert.getSubjectX500Principal().getName();

                for (X509Certificate otherCert : x509Certs) {
                    if (cert != otherCert) {
                        String otherIssuer = otherCert.getIssuerX500Principal().getName();
                        if (certSubject.equals(otherIssuer)) {
                            isIssuerOfOther = true;
                            break;
                        }
                    }
                }

                if (!isIssuerOfOther) {
                    leafCert = cert;
                    break;
                }
            }

            if (leafCert == null) {
                // Couldn't identify leaf certificate, return original order
                return certificates;
            }

            // Build the chain from leaf to root
            List<Certificate> orderedChain = new ArrayList<>();
            X509Certificate current = leafCert;

            while (current != null && orderedChain.size() < x509Certs.length) {
                orderedChain.add(current);

                // Find the issuer of the current certificate
                String issuerDN = current.getIssuerX500Principal().getName();
                String currentSubjectDN = current.getSubjectX500Principal().getName();

                // Check if this is a self-signed certificate (root CA)
                if (issuerDN.equals(currentSubjectDN)) {
                    // Self-signed, we've reached the root
                    break;
                }

                // Look for the issuer in the certificate chain
                X509Certificate issuer = subjectToCert.get(issuerDN);
                if (issuer == null || issuer == current) {
                    // No issuer found in chain, or circular reference
                    break;
                }

                current = issuer;
            }

            // Convert back to Certificate array
            return orderedChain.toArray(new Certificate[0]);

        } catch (Exception e) {
            // If any error occurs during ordering, return original order
            return certificates;
        }
    }

}
