// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
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
import javax.security.auth.x500.X500Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

public final class CertificateUtil {
    private static final Logger LOGGER = Logger.getLogger(CertificateUtil.class.getName());
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
        certificates = orderCertificateChain(certificates);
        // Complete the chain by downloading any missing intermediate CA certificates via the AIA extension.
        // This handles the case where only the leaf certificate was stored in Azure Key Vault
        // (e.g. a non-exportable certificate where the caller only merged the leaf cert during CSR completion).
        certificates = completeChainViaAia(certificates);
        return certificates;
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

            // Create a map of subject X500Principal to list of certificates
            // This preserves multiple certs with the same subject DN (e.g. cross-signed roots)
            Map<X500Principal, List<X509Certificate>> subjectToCerts = new HashMap<>();
            for (X509Certificate cert : x509Certs) {
                X500Principal subject = cert.getSubjectX500Principal();
                subjectToCerts.computeIfAbsent(subject, k -> new ArrayList<>()).add(cert);
            }

            // Find the end-entity (leaf) certificate
            // It's the one that is not the issuer of any other certificate in the chain
            X509Certificate leafCert = null;
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

            while (orderedChain.size() < x509Certs.length) {
                orderedChain.add(current);

                // Find the issuer of the current certificate
                X500Principal issuerPrincipal = current.getIssuerX500Principal();
                X500Principal currentSubjectPrincipal = current.getSubjectX500Principal();

                // Check if this is a self-signed certificate (root CA)
                if (issuerPrincipal.equals(currentSubjectPrincipal)) {
                    // Self-signed, we've reached the root
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

            // Convert back to Certificate array
            Certificate[] result = orderedChain.toArray(new Certificate[0]);

            // Log the ordered chain for debugging
            if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
                logCertificateChain("Certificate chain after ordering", result);
            }

            return result;

        } catch (Exception e) {
            // If any error occurs during ordering, return original order
            return certificates;
        }
    }

    /**
     * Completes an incomplete certificate chain by downloading missing intermediate CA certificates
     * using the AIA (Authority Information Access) extension embedded in each certificate.
     *
     * <p>This is needed when Azure Key Vault's secrets endpoint returns only the leaf certificate
     * (e.g. when the caller merged only the leaf cert during CSR completion for a non-exportable key).
     * Without the intermediate CA certificates, jarsigner cannot build a valid PKIX path to a trusted
     * root CA, producing "PKIX path building failed" warnings on verify.
     *
     * <p>The method walks up the chain starting from the current top certificate. If that certificate
     * is not self-signed (i.e. it is not a root CA) and its issuer is not already present in the chain,
     * it fetches the issuer certificate from the {@code caIssuers} URL in the certificate's AIA extension.
     * This process repeats until the chain reaches a self-signed root CA, no more AIA URLs are found, or
     * the safety download limit is reached.
     *
     * @param orderedCertificates certificate array already ordered leaf → intermediate(s) → root
     * @return the (potentially extended) certificate array with missing intermediates appended
     */
    static Certificate[] completeChainViaAia(Certificate[] orderedCertificates) {
        if (orderedCertificates == null || orderedCertificates.length == 0) {
            return orderedCertificates;
        }

        List<Certificate> chain = new ArrayList<>(Arrays.asList(orderedCertificates));
        int maxDownloads = 10; // Safety limit to prevent infinite loops

        while (maxDownloads-- > 0) {
            Certificate top = chain.get(chain.size() - 1);
            if (!(top instanceof X509Certificate)) {
                break;
            }
            X509Certificate x509Top = (X509Certificate) top;

            // Chain is complete once the top cert is actually self-signed (verified by signature)
            if (isSelfSignedCertificate(x509Top)) {
                LOGGER.log(FINE, "Certificate chain is complete. Root CA: {0}",
                    x509Top.getSubjectX500Principal().getName());
                break;
            }

            // Try to download the issuer certificate via the AIA extension
            X509Certificate issuer = downloadIssuerCertificateFromAia(x509Top);
            if (issuer == null) {
                LOGGER.log(FINE, "Could not download issuer certificate for [{0}] via AIA extension. "
                    + "Certificate chain may be incomplete.", x509Top.getSubjectX500Principal().getName());
                break;
            }

            // Validate: the downloaded cert's subject must match the expected issuer DN
            // AND verify that it can actually sign the current certificate (issuer validation)
            X500Principal expectedIssuerPrincipal = x509Top.getIssuerX500Principal();
            X500Principal issuerPrincipal = issuer.getSubjectX500Principal();
            if (!issuerPrincipal.equals(expectedIssuerPrincipal)) {
                LOGGER.log(WARNING,
                    "Downloaded certificate subject [{0}] does not match expected issuer DN [{1}]. "
                        + "Ignoring and stopping AIA chain completion.",
                    new Object[] { issuerPrincipal.getName(), expectedIssuerPrincipal.getName() });
                break;
            }

            // Verify that the downloaded certificate is a CA and can verify the current certificate's signature
            if (!isValidIssuer(issuer, x509Top)) {
                LOGGER.log(WARNING,
                    "Downloaded certificate cannot verify signature on current certificate or is not a CA. "
                        + "Stopping AIA chain completion.");
                break;
            }

            // Avoid duplicates: a cert with the same subject is already in the chain
            boolean isDuplicate = chain.stream()
                .filter(c -> c instanceof X509Certificate)
                .anyMatch(
                    c -> ((X509Certificate) c).getSubjectX500Principal().equals(issuer.getSubjectX500Principal()));
            if (isDuplicate) {
                LOGGER.log(FINE, "Certificate [{0}] is already in the chain. Stopping AIA download.",
                    issuer.getSubjectX500Principal().getName());
                break;
            }

            LOGGER.log(FINE, "Downloaded intermediate CA certificate via AIA: {0}",
                issuer.getSubjectX500Principal().getName());
            chain.add(issuer);
        }

        Certificate[] result = chain.toArray(new Certificate[0]);

        // Log the completed chain for debugging
        if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
            logCertificateChain("Certificate chain after AIA completion", result);
        }

        return result;
    }

    /**
     * Logs the certificate chain for debugging purposes.
     *
     * @param label a descriptive label for the log
     * @param certificates the certificate array to log
     */
    private static void logCertificateChain(String label, Certificate[] certificates) {
        if (certificates == null || certificates.length == 0) {
            LOGGER.log(FINE, "{0}: empty chain", label);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(label).append(" [").append(certificates.length).append(" certs]:\n");

        for (int i = 0; i < certificates.length; i++) {
            if (certificates[i] instanceof X509Certificate) {
                X509Certificate x509 = (X509Certificate) certificates[i];
                String subject = x509.getSubjectX500Principal().getName();
                String issuer = x509.getIssuerX500Principal().getName();
                boolean isSelfSigned = subject.equals(issuer);

                sb.append("  [")
                    .append(i)
                    .append("] Subject: ")
                    .append(subject)
                    .append(" | Issuer: ")
                    .append(issuer)
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
    private static boolean isSelfSignedCertificate(X509Certificate cert) {
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
     * Verifies that an issuer certificate is valid for signing the given certificate.
     * Checks:
     * 1. The issuer's subject matches the certificate's issuer DN
     * 2. The issuer can verify the certificate's signature
     * 3. The issuer is a CA (has CA constraint or is self-signed)
     *
     * @param issuer the potential issuer certificate
     * @param cert the certificate to verify
     * @return true if the issuer certificate is valid, false otherwise
     */
    private static boolean isValidIssuer(X509Certificate issuer, X509Certificate cert) {
        try {
            // Verify the certificate's signature using the issuer's public key
            cert.verify(issuer.getPublicKey());

            // Check if the issuer is a CA (either self-signed or has CA basic constraints)
            // A root CA is self-signed, intermediate CAs should have basicConstraints.CA=true
            boolean isCA = isSelfSignedCertificate(issuer) || (issuer.getBasicConstraints() >= 0); // basicConstraints >= 0 means CA is true

            return isCA;
        } catch (Exception e) {
            // If signature verification fails or any error occurs, it's not a valid issuer
            return false;
        }
    }

    /**
     * Downloads the issuer certificate for the given certificate using the CA Issuers URL
     * found in the certificate's AIA (Authority Information Access) extension.
     *
     * @param cert the certificate whose issuer should be downloaded
     * @return the issuer {@link X509Certificate}, or {@code null} if it cannot be retrieved
     */
    static X509Certificate downloadIssuerCertificateFromAia(X509Certificate cert) {
        try {
            byte[] aiaValue = cert.getExtensionValue(Extension.authorityInfoAccess.getId());
            if (aiaValue == null) {
                return null;
            }

            // getExtensionValue() wraps the value in an OCTET STRING; unwrap it first
            ASN1OctetString octStr = ASN1OctetString.getInstance(aiaValue);
            AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(octStr.getOctets());

            for (AccessDescription ad : aia.getAccessDescriptions()) {
                // id-ad-caIssuers (1.3.6.1.5.5.7.48.2) points to the issuer's certificate
                if (!X509ObjectIdentifiers.id_ad_caIssuers.equals(ad.getAccessMethod())) {
                    continue;
                }
                GeneralName location = ad.getAccessLocation();
                if (location.getTagNo() != GeneralName.uniformResourceIdentifier) {
                    continue;
                }
                String url = location.getName().toString();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    continue; // Only HTTP/HTTPS URLs are supported
                }

                LOGGER.log(FINE, "Downloading issuer certificate from AIA URL: {0}", url);
                byte[] certBytes = HttpUtil.getBytes(url);
                if (certBytes == null) {
                    LOGGER.log(FINE, "Failed to download issuer certificate from AIA URL: {0}", url);
                    continue;
                }

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                try {
                    // CA certs from AIA are typically DER-encoded
                    Certificate downloaded = cf.generateCertificate(new ByteArrayInputStream(certBytes));
                    if (downloaded instanceof X509Certificate) {
                        return (X509Certificate) downloaded;
                    }
                } catch (CertificateException e) {
                    // Fall back to PEM format
                    String pem = new String(certBytes, StandardCharsets.UTF_8);
                    if (pem.contains(BEGIN_CERTIFICATE)) {
                        Certificate[] pemCerts = loadCertificatesFromSecretBundleValuePem(pem);
                        if (pemCerts.length > 0 && pemCerts[0] instanceof X509Certificate) {
                            return (X509Certificate) pemCerts[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(FINE, "Failed to download issuer certificate from AIA extension.", e);
        }
        return null;
    }

}
