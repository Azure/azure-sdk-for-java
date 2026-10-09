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
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;

public final class CertificateUtil {
    private static final Logger LOGGER = Logger.getLogger(CertificateUtil.class.getName());
    static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    /**
     * Loads certificates from a Key Vault secret bundle value.
     *
     * @param string The secret bundle value.
     * @param disableAiaDownload Indicates if AIA certificate downloads should be disabled.
     * @return The loaded certificate chain.
     * @throws CertificateException If a certificate cannot be parsed.
     * @throws IOException If the secret bundle cannot be read.
     * @throws KeyStoreException If the PKCS12 key store cannot be loaded.
     * @throws NoSuchAlgorithmException If a required algorithm is unavailable.
     * @throws NoSuchProviderException If a required provider is unavailable.
     * @throws PKCSException If the PKCS data cannot be parsed.
     */
    public static Certificate[] loadCertificatesFromSecretBundleValue(String string, boolean disableAiaDownload)
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException,
        PKCSException {
        Certificate[] certificates;
        if (string.contains(BEGIN_CERTIFICATE)) {
            certificates = loadCertificatesFromSecretBundleValuePem(string);
        } else {
            certificates = loadCertificatesFromSecretBundleValuePKCS12(string);
        }

        // Ensure certificates are in the correct order: end-entity (leaf) → intermediate(s) → root CA
        // This is required for jarsigner and other Java security tools
        certificates = orderCertificateChain(certificates);

        // A contiguous chain may still be missing issuers above its terminal certificate. Resolution remains
        // cache-first, and a chain ending in a self-signed root does not enter the AIA completion path.
        if (AiaCertificateChainUtil.shouldCompleteChainViaAia(certificates)) {
            LOGGER.log(FINE, "Certificate chain requires AIA completion; ordered chain contains {0} certificate(s).",
                certificates.length);
            certificates = AiaCertificateChainUtil.completeChainViaAia(certificates, disableAiaDownload);
        } else {
            LOGGER.log(FINE,
                "Certificate chain does not require AIA completion; ordered chain contains {0} " + "certificate(s).",
                certificates.length);
        }

        return certificates;
    }

    private static Certificate[] loadCertificatesFromSecretBundleValuePem(InputStream inputStream)
        throws IOException, CertificateException {
        List<Certificate> certificates = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        boolean certificateBlockOpen = false;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(BEGIN_CERTIFICATE)) {
                if (certificateBlockOpen) {
                    throw new CertificateException("Certificate PEM block contains a nested BEGIN CERTIFICATE marker.");
                }
                certificateBlockOpen = true;
                builder = new StringBuilder();
            }
            if (line.contains(END_CERTIFICATE)) {
                if (!certificateBlockOpen) {
                    throw new CertificateException(
                        "Certificate PEM block has an END CERTIFICATE marker without a matching BEGIN CERTIFICATE marker.");
                }
                builder.append(line).append('\n');
                InputStream stream = new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
                Certificate certificate = factory.generateCertificate(stream);
                certificates.add(certificate);
                certificateBlockOpen = false;
            } else if (certificateBlockOpen) {
                builder.append(line).append('\n');
            }
        }
        if (certificateBlockOpen) {
            throw new CertificateException("Certificate PEM block is not terminated.");
        }
        return certificates.toArray(new Certificate[0]);
    }

    static Certificate[] loadCertificatesFromSecretBundleValuePem(String string)
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
        return factory.generateCertificates(inputStream).toArray(new Certificate[0]);
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

            // Create a map of subject X500Principal to list of certificates
            // This preserves multiple certs with the same subject DN (e.g. cross-signed roots)
            Map<X500Principal, List<X509Certificate>> subjectToCerts = new HashMap<>();
            for (X509Certificate cert : x509Certs) {
                X500Principal subject = cert.getSubjectX500Principal();
                subjectToCerts.computeIfAbsent(subject, k -> new ArrayList<>()).add(cert);
            }

            // Find the end-entity (leaf) certificate
            // Prioritize: a cert whose issuer exists in the chain (true end-entity), otherwise not an issuer of others
            // Avoid: selecting a self-signed root as leaf if a true leaf with missing issuer exists
            X509Certificate leafCert = null;
            X509Certificate selfSignedFallback = null;

            for (X509Certificate cert : x509Certs) {
                boolean isIssuerOfOther = false;
                X500Principal certSubject = cert.getSubjectX500Principal();

                for (X509Certificate otherCert : x509Certs) {
                    if (cert != otherCert) {
                        X500Principal otherIssuer = otherCert.getIssuerX500Principal();
                        if (certSubject.equals(otherIssuer)) {
                            isIssuerOfOther = true;
                            break;
                        }
                    }
                }

                if (!isIssuerOfOther) {
                    // This cert is not the issuer of any other cert in the chain
                    X500Principal issuerPrincipal = cert.getIssuerX500Principal();

                    if (!isSelfSignedCertificate(cert)) {
                        // Non-self-signed cert: check if issuer exists in the chain AND can verify signature
                        List<X509Certificate> potentialIssuers = subjectToCerts.get(issuerPrincipal);
                        if (potentialIssuers != null) {
                            // Check if any potential issuer can actually verify this cert's signature
                            for (X509Certificate potentialIssuer : potentialIssuers) {
                                if (isValidIssuer(potentialIssuer, cert)) {
                                    // Valid issuer found and verified, this is a true leaf/end-entity
                                    leafCert = cert;
                                    break;
                                }
                            }
                            if (leafCert != null) {
                                break; // Found valid issuer, stop searching
                            }
                        }
                        if (leafCert == null) {
                            // No verified issuer in chain and not self-signed = true leaf with missing intermediate
                            leafCert = cert;
                        }
                    } else if (selfSignedFallback == null) {
                        // Self-signed cert with no issuer in chain = likely a root, remember as fallback
                        selfSignedFallback = cert;
                    }
                }
            }

            // Use fallback (self-signed root) only if no non-self-signed leaf was found
            if (leafCert == null) {
                leafCert = selfSignedFallback;
            }

            if (leafCert == null) {
                // Couldn't identify leaf certificate, return original order
                return certificates;
            }

            // Build the chain from leaf to root
            List<Certificate> orderedChain = new ArrayList<>();
            X509Certificate current = leafCert;

            while (orderedChain.size() < x509Certs.length) {
                orderedChain.add(current);

                // Find the issuer of the current certificate
                X500Principal issuerPrincipal = current.getIssuerX500Principal();

                // Check if this is actually a self-signed certificate (root CA) by verifying signature
                if (isSelfSignedCertificate(current)) {
                    // Truly self-signed, we've reached the root
                    break;
                }

                // Look for the issuer in the certificate chain
                // There may be multiple candidates with the same subject DN (e.g. cross-signed roots)
                List<X509Certificate> issuerCandidates = subjectToCerts.get(issuerPrincipal);
                X509Certificate issuer = null;

                if (issuerCandidates != null) {
                    // Find the first candidate that can actually verify the current certificate's signature
                    // Use a final reference for use in lambda expressions
                    final X509Certificate currentCert = current;
                    issuer = issuerCandidates.stream()
                        .filter(candidate -> candidate != currentCert)
                        .filter(candidate -> isValidIssuer(candidate, currentCert))
                        .findFirst()
                        .orElse(null);
                }

                if (issuer == null) {
                    // No valid issuer found in chain
                    break;
                }

                current = issuer;
            }

            // Append any certificates that were not placed in the ordered chain
            // (e.g. cross-signed roots or unrelated intermediates) so nothing is silently dropped.
            for (X509Certificate cert : x509Certs) {
                if (!orderedChain.contains(cert)) {
                    orderedChain.add(cert);
                }
            }

            // Convert back to Certificate array
            Certificate[] result = orderedChain.toArray(new Certificate[0]);

            // Log the ordered chain for debugging
            if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
                logCertificateChain("Certificate chain after ordering", result);
            }

            return result;

        } catch (Exception e) {
            // If any error occurs during ordering, log it and return original order
            // This prevents silently hiding certificate chain issues in production
            LOGGER.log(FINE,
                "Failed to order certificate chain. Returning original order. This may cause jarsigner PKIX issues.",
                e);
            return certificates;
        }
    }

    /**
     * Logs the certificate chain for debugging purposes.
     *
     * @param label a descriptive label for the log
     * @param certificates the certificate array to log
     */
    static void logCertificateChain(String label, Certificate[] certificates) {
        if (certificates == null || certificates.length == 0) {
            LOGGER.log(FINE, "{0}: empty chain", label);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(label).append(" [").append(certificates.length).append(" certs]:\n");

        for (int i = 0; i < certificates.length; i++) {
            if (certificates[i] instanceof X509Certificate) {
                X509Certificate x509 = (X509Certificate) certificates[i];
                X500Principal subject = x509.getSubjectX500Principal();
                X500Principal issuer = x509.getIssuerX500Principal();
                boolean isSelfSigned = isSelfSignedCertificate(x509);

                sb.append("  [")
                    .append(i)
                    .append("] Subject: ")
                    .append(subject.getName())
                    .append(" | Issuer: ")
                    .append(issuer.getName())
                    .append(" | Self-Signed: ")
                    .append(isSelfSigned)
                    .append("\n");
            } else {
                sb.append("  [").append(i).append("] Non-X509 certificate\n");
            }
        }

        LOGGER.log(FINE, sb.toString());
    }

    /**
     * Verifies whether a certificate is self-signed (signed by its own private key).
     * This is checked by verifying the certificate's signature using its own public key.
     *
     * @param cert the certificate to verify
     * @return true if the certificate is self-signed, false otherwise
     */
    static boolean isSelfSignedCertificate(X509Certificate cert) {
        // First check: subject and issuer must be the same
        if (!cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
            return false;
        }

        // Second check: verify the signature using its own public key
        try {
            cert.verify(cert.getPublicKey());
            return true;
        } catch (Exception e) {
            // If signature verification fails, it's not self-signed
            return false;
        }
    }

    /**
     * Validates that an issuer certificate is legitimate for signing another certificate.
     *
     * <p>This method performs the following checks:
     * <ol>
     *   <li>Verifies that the signature on the certificate was created by the issuer's private key</li>
     *   <li>Verifies that the issuer is authorized to be a CA
     *       (either self-signed root or has CA bit set in basicConstraints)</li>
     *   <li>Verifies that, when a KeyUsage extension is present, the keyCertSign bit is set (RFC 5280)</li>
     * </ol>
     *
     * <p>The issuer's validity period is deliberately not checked here, because this method also decides how a
     * chain returned by Azure Key Vault is ordered and how far it can be walked. Rejecting an expired certificate
     * at that point would reorder existing chains and trigger downloads that were previously never performed.
     * Certificates entering the chain from the network are checked separately by {@code AiaCertificateChainUtil}.
     *
     * @param issuer the potential issuer certificate
     * @param cert the certificate to verify
     * @return true if the issuer certificate can validly issue the certificate, false otherwise
     */
    static boolean isValidIssuer(X509Certificate issuer, X509Certificate cert) {
        try {
            // Verify the certificate's signature using the issuer's public key
            cert.verify(issuer.getPublicKey());

            // Check if the issuer is a CA (either self-signed or has CA basic constraints)
            // A root CA is self-signed, intermediate CAs should have basicConstraints.CA=true
            // basicConstraints >= 0 means CA is true
            boolean isCA = isSelfSignedCertificate(issuer) || (issuer.getBasicConstraints() >= 0);
            if (!isCA) {
                return false;
            }

            // RFC 5280: if KeyUsage is present for a CA certificate, keyCertSign must be set.
            boolean[] keyUsage = issuer.getKeyUsage();
            if (keyUsage != null) {
                // keyCertSign is bit 5; if missing or false, the cert must not issue other certs.
                if (keyUsage.length <= 5 || !keyUsage[5]) {
                    return false;
                }
            }

            return true;
        } catch (GeneralSecurityException e) {
            // If signature verification fails or any error occurs, it's not a valid issuer
            return false;
        }
    }
}
