// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * Completes an incomplete certificate chain with the issuer certificates published in the AIA (Authority Information
 * Access) extension of the certificates it already holds.
 *
 * <p>Azure Key Vault's secrets endpoint returns only the leaf certificate for a non-exportable certificate whose
 * caller merged just the leaf during CSR completion. Without the intermediate CA certificates, jarsigner cannot build
 * a valid PKIX path to a trusted root CA and reports "PKIX path building failed" on verify.
 *
 * <p><strong>Security note:</strong> completion issues outbound HTTP(S) requests to URLs embedded in certificates.
 * Set the system property {@code azure.keyvault.jca.disable-aia-download=true} to disable it in locked-down
 * environments or when loading untrusted certificates.
 */
final class AiaCertificateChainUtil {
    private static final Logger LOGGER = Logger.getLogger(AiaCertificateChainUtil.class.getName());
    static final String DISABLE_AIA_DOWNLOAD_PROPERTY = "azure.keyvault.jca.disable-aia-download";
    private static final int AIA_CACHE_MAX_SIZE = 32;
    private static final long AIA_CACHE_TTL_IN_MILLIS = TimeUnit.HOURS.toMillis(24);
    // Issuer certificates are immutable, so caching them per CA Issuers URL avoids re-downloading the same
    // certificate for every alias sharing an issuer and on every certificates refresh cycle.
    private static final Map<String, CachedAiaResponse> AIA_CACHE = new ConcurrentHashMap<>();

    /**
     * Determines whether a certificate chain has to be completed with issuer certificates downloaded via AIA.
     *
     * <p>Completion is only required when the chain cannot be walked from the leaf upwards: either Azure Key Vault
     * returned a leaf-only bundle (the non-exportable case behind the {@code jarsigner} PKIX failure), or an
     * intermediate CA is missing in the middle of the chain. A contiguous chain is left untouched: {@code jarsigner}
     * and PKIX path building only need the path up to a trust anchor, and the root CA already is a trust anchor in
     * the trust store, so it does not have to be embedded in the chain.
     *
     * <p><strong>Known limitation:</strong> a chain whose missing link sits above its last certificate is reported
     * as complete. A multi-level PKI returning {@code [leaf, intermediate1]} while {@code intermediate2} is also
     * required looks contiguous, so no download is attempted even though PKIX path building can still fail.
     * Detecting that case would require an AIA download on every certificate load, which is the network dependency
     * this check exists to avoid; such deployments should merge the full chain into the Key Vault certificate.
     *
     * @param certificates the ordered certificate chain
     * @return true if the chain is leaf-only or has a broken issuer link, false if it is contiguous or empty
     */
    static boolean isChainIncomplete(Certificate[] certificates) {
        if (certificates == null || certificates.length == 0) {
            return false;
        }

        // A leaf-only chain is contiguous by definition, hence the explicit check.
        return certificates.length == 1 || findValidChainEnd(Arrays.asList(certificates)) < certificates.length - 1;
    }

    /**
     * Completes an incomplete certificate chain by downloading missing intermediate CA certificates
     * using the AIA (Authority Information Access) extension embedded in each certificate.
     *
     * <p>Because completion issues outbound HTTP requests, callers must restrict it to chains that need it
     * (see {@link #isChainIncomplete(Certificate[])}).
     *
     * <p>The method walks up the contiguous issuer path (leaf → intermediate → root) starting from
     * the first certificate, downloading missing intermediates via AIA. Downloaded issuers are inserted
     * immediately after the current end of the valid chain (before any unplaced/extra certificates).
     * This process repeats until the chain reaches a self-signed root CA, no more AIA URLs are found, or
     * the safety download limit is reached.
     *
     * @param orderedCertificates certificate array with contiguous issuer path + any unplaced certs appended
     * @return the (potentially extended) certificate array with missing intermediates inserted in the valid chain
     */
    static Certificate[] completeChainViaAia(Certificate[] orderedCertificates) {
        if (orderedCertificates == null || orderedCertificates.length == 0) {
            return orderedCertificates;
        }

        // Check if AIA downloading is disabled by system property
        String disableAiaDownload = System.getProperty(DISABLE_AIA_DOWNLOAD_PROPERTY);
        if ("true".equalsIgnoreCase(disableAiaDownload)) {
            LOGGER.log(FINE, "AIA chain completion is disabled by system property [{0}]",
                DISABLE_AIA_DOWNLOAD_PROPERTY);
            return orderedCertificates;
        }

        List<Certificate> chain = new ArrayList<>(Arrays.asList(orderedCertificates));
        int maxDownloads = 10; // Safety limit to prevent infinite loops
        // Defence in depth. Repositioning below takes `continue` without decrementing maxDownloads, so it is the one
        // path whose termination rests on findValidChainEnd() advancing. It does advance today, because a reposition
        // only touches positions after the valid prefix, but this cap keeps the loop bounded should a later change
        // break that invariant. The bound is generous so it never truncates a legitimately completable chain.
        int remainingIterations = 4 * (chain.size() + maxDownloads) + 16;

        while (true) {
            if (--remainingIterations < 0) {
                LOGGER.log(FINE, "Reached maximum certificate chain-completion iterations. Stopping to guard against "
                    + "non-terminating input (possible duplicate or cross-signed intermediates).");
                break;
            }

            // Find the end of the valid chain (continuous issuer path leaf → issuer → ...).
            // This excludes any extra/unplaced certificates appended at the end.
            int validChainEnd = findValidChainEnd(chain);
            if (validChainEnd < 0) {
                // Empty chain, stop
                break;
            }

            Certificate topOfValidChain = chain.get(validChainEnd);
            if (!(topOfValidChain instanceof X509Certificate)) {
                break;
            }
            X509Certificate x509Top = (X509Certificate) topOfValidChain;

            // Chain is complete once the top cert is actually self-signed (verified by signature)
            if (CertificateUtil.isSelfSignedCertificate(x509Top)) {
                LOGGER.log(FINE, "Certificate chain is complete. Root CA: {0}",
                    x509Top.getSubjectX500Principal().getName());
                break;
            }

            // Check if a valid issuer for x509Top already exists anywhere in the chain.
            // We check *validity* (signature + CA capability), not just subject DN equality,
            // because re-issued or cross-signed intermediates may share the same subject DN
            // but have a different key and therefore cannot validate x509Top's signature.
            X509Certificate validIssuerInChain = null;
            int validIssuerIndex = -1;
            for (int i = 0; i < chain.size(); i++) {
                Certificate cert = chain.get(i);
                if (cert instanceof X509Certificate) {
                    X509Certificate candidate = (X509Certificate) cert;
                    if (candidate.getSubjectX500Principal().equals(x509Top.getIssuerX500Principal())
                        && CertificateUtil.isValidIssuer(candidate, x509Top)) {
                        validIssuerInChain = candidate;
                        validIssuerIndex = i;
                        LOGGER.log(FINE, "Valid issuer [{0}] already present in chain at index {1}.",
                            new Object[] { candidate.getSubjectX500Principal().getName(), i });
                        break;
                    }
                }
            }

            if (validIssuerInChain != null) {
                if (validIssuerIndex > validChainEnd + 1) {
                    // Valid issuer sits among the appended/unplaced certs after the valid prefix.
                    // Moving it up into the contiguous slot is safe: the removal is beyond the valid
                    // prefix, so it cannot break an earlier link. This makes forward progress, so
                    // re-evaluate the chain from the top.
                    LOGGER.log(FINE, "Valid issuer found but not at contiguous position. Moving from index {0} to {1}.",
                        new Object[] { validIssuerIndex, validChainEnd + 1 });
                    chain.remove(validIssuerIndex);
                    chain.add(validChainEnd + 1, validIssuerInChain);
                    continue;
                } else if (validIssuerIndex <= validChainEnd) {
                    // The matching issuer lies *inside* the already-valid prefix. This can occur with
                    // duplicate or cross-signed intermediates that share a subject DN and key. Removing
                    // it would break the valid prefix and could keep the loop oscillating between two
                    // chain arrangements without ever decrementing maxDownloads, so do NOT reposition here.
                    // Fall through to the bounded AIA download branch, which inserts a fresh issuer copy
                    // contiguously and makes guaranteed forward progress.
                    LOGGER.log(FINE,
                        "Valid issuer for [{0}] found inside the valid prefix at index {1} (likely duplicate or "
                            + "cross-signed). Not repositioning; attempting AIA download instead.",
                        new Object[] { x509Top.getSubjectX500Principal().getName(), validIssuerIndex });
                } else {
                    // validIssuerIndex == validChainEnd + 1: already contiguous. findValidChainEnd would
                    // normally have consumed it already; fall through rather than spinning on `continue`.
                    LOGGER.log(FINE, "Valid issuer already at correct contiguous position.");
                }
                // Fall through to the AIA download branch below.
            }

            // Try to download the issuer certificate via the AIA extension.
            // Decrement maxDownloads for each attempted issuer resolution to avoid infinite loops.
            if (--maxDownloads < 0) {
                LOGGER.log(FINE, "Reached maximum AIA download attempts ({0}). Certificate chain may be incomplete.",
                    10);
                break;
            }

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
            if (!CertificateUtil.isValidIssuer(issuer, x509Top)) {
                LOGGER.log(WARNING,
                    "Downloaded certificate cannot verify signature on current certificate or is not a CA. "
                        + "Stopping AIA chain completion.");
                break;
            }

            LOGGER.log(FINE, "Downloaded intermediate CA certificate via AIA: {0}",
                issuer.getSubjectX500Principal().getName());
            // Insert the downloaded issuer immediately after the valid chain end, before any extra certs
            chain.add(validChainEnd + 1, issuer);
        }

        Certificate[] result = chain.toArray(new Certificate[0]);

        // Log the completed chain for debugging
        if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
            CertificateUtil.logCertificateChain("Certificate chain after AIA completion", result);
        }

        return result;
    }

    /**
     * Finds the end position of the valid (contiguous) issuer chain.
     * Starting from position 0, walks the chain as long as the next certificate is the issuer of the current one.
     * Stops at the first position where the issuer relationship breaks or at a self-signed certificate.
     *
     * @param chain the certificate chain
     * @return the index of the last certificate in the valid chain, or -1 if empty
     */
    private static int findValidChainEnd(List<Certificate> chain) {
        if (chain == null || chain.isEmpty()) {
            return -1;
        }

        int pos = 0;
        while (pos < chain.size()) {
            Certificate cert = chain.get(pos);
            if (!(cert instanceof X509Certificate)) {
                // Stop at non-X509 certificate
                break;
            }

            X509Certificate x509Cert = (X509Certificate) cert;

            // If this is the last certificate, it's the end of the valid chain
            if (pos == chain.size() - 1) {
                return pos;
            }

            // Check if the next certificate is the issuer of this one
            Certificate nextCert = chain.get(pos + 1);
            if (!(nextCert instanceof X509Certificate)) {
                // Next cert is not X509, stop here
                return pos;
            }

            X509Certificate nextX509Cert = (X509Certificate) nextCert;
            X500Principal issuerPrincipal = x509Cert.getIssuerX500Principal();
            X500Principal nextSubjectPrincipal = nextX509Cert.getSubjectX500Principal();

            if (!issuerPrincipal.equals(nextSubjectPrincipal)) {
                // Issuer relationship broken, stop here
                return pos;
            }

            // Verify that next cert can actually sign this one
            if (!CertificateUtil.isValidIssuer(nextX509Cert, x509Cert)) {
                // Next cert cannot validate this cert's signature, stop here
                return pos;
            }

            // If this cert is self-signed, it's the end of the chain
            if (CertificateUtil.isSelfSignedCertificate(x509Cert)) {
                return pos;
            }

            pos++;
        }

        return pos > 0 ? pos - 1 : 0;
    }

    /**
     * Downloads the issuer certificate for the given certificate using the CA Issuers URL
     * found in the certificate's AIA (Authority Information Access) extension.
     *
     * <p>A downloaded certificate is only accepted when its subject matches the expected issuer DN, it is currently
     * within its validity period, and it can validly issue the given certificate.
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

                X500Principal expectedIssuerPrincipal = cert.getIssuerX500Principal();
                for (X509Certificate candidate : fetchCertificatesFromAiaUrl(url)) {
                    // Validation runs on every use, including cache hits, so a cached certificate can never
                    // shortcut subject, validity or issuer verification.
                    if (expectedIssuerPrincipal.equals(candidate.getSubjectX500Principal())
                        && isCurrentlyValid(candidate)
                        && CertificateUtil.isValidIssuer(candidate, cert)) {
                        return candidate;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(FINE, "Failed to download issuer certificate from AIA extension.", e);
        }
        return null;
    }

    /**
     * Retrieves the certificates published at a CA Issuers URL, reusing a previously cached response when possible.
     *
     * <p>Issuer certificates are immutable, so downloading them once per URL removes repeated round trips to public
     * CA endpoints across aliases sharing an issuer and across certificate refresh cycles. Only the parsed
     * certificates are cached, never the result of validating them against a specific certificate: callers must
     * still run {@code CertificateUtil.isValidIssuer} on every use.
     *
     * @param url the CA Issuers URL taken from an AIA extension
     * @return the certificates published at the URL, or an empty list if they cannot be retrieved or parsed
     */
    static List<X509Certificate> fetchCertificatesFromAiaUrl(String url) {
        CachedAiaResponse cachedResponse = AIA_CACHE.get(url);
        if (cachedResponse != null && !cachedResponse.isExpired()) {
            LOGGER.log(FINE, "Reusing the cached AIA response for URL: {0}", url);
            return cachedResponse.certificates;
        }

        LOGGER.log(FINE, "Downloading issuer certificate from AIA URL: {0}", url);
        byte[] certBytes = HttpUtil.getBytes(url);
        if (certBytes == null) {
            LOGGER.log(FINE, "Failed to download issuer certificate from AIA URL: {0}", url);
            return Collections.emptyList();
        }

        List<X509Certificate> certificates = parseCertificates(certBytes);
        if (!certificates.isEmpty()) {
            cacheAiaResponse(url, certificates);
        }

        return certificates;
    }

    /**
     * Clears the cached AIA responses.
     *
     * <p>Used by tests to keep certificate downloads isolated from each other.
     */
    static void clearAiaCache() {
        AIA_CACHE.clear();
    }

    /**
     * Parses the certificates contained in an AIA response, which may be DER- or PEM-encoded and may hold a bundle
     * rather than a single certificate.
     *
     * @param certBytes the raw AIA response body
     * @return the parsed certificates, or an empty list if the response cannot be parsed
     */
    private static List<X509Certificate> parseCertificates(byte[] certBytes) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return toX509Certificates(cf.generateCertificates(new ByteArrayInputStream(certBytes)));
        } catch (CertificateException e) {
            // Fall back to PEM format
            String pem = new String(certBytes, StandardCharsets.UTF_8);
            if (pem.contains(CertificateUtil.BEGIN_CERTIFICATE)) {
                try {
                    return toX509Certificates(
                        Arrays.asList(CertificateUtil.loadCertificatesFromSecretBundleValuePem(pem)));
                } catch (IOException | CertificateException pemException) {
                    LOGGER.log(FINE, "Failed to parse the AIA response as PEM.", pemException);
                }
            }
        }

        return Collections.emptyList();
    }

    private static List<X509Certificate> toX509Certificates(Collection<? extends Certificate> certificates) {
        List<X509Certificate> x509Certificates = new ArrayList<>(certificates.size());
        for (Certificate certificate : certificates) {
            if (certificate instanceof X509Certificate) {
                x509Certificates.add((X509Certificate) certificate);
            }
        }

        return Collections.unmodifiableList(x509Certificates);
    }

    /**
     * Caches an AIA response, keeping the cache bounded so that certificates advertising many distinct AIA URLs
     * cannot grow it without limit.
     *
     * @param url the CA Issuers URL the certificates were published at
     * @param certificates the certificates parsed from the response
     */
    private static void cacheAiaResponse(String url, List<X509Certificate> certificates) {
        if (AIA_CACHE.size() >= AIA_CACHE_MAX_SIZE) {
            AIA_CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());

            if (AIA_CACHE.size() >= AIA_CACHE_MAX_SIZE) {
                LOGGER.log(FINE, "The AIA response cache reached its maximum size of {0} entries. Clearing it.",
                    AIA_CACHE_MAX_SIZE);
                AIA_CACHE.clear();
            }
        }

        AIA_CACHE.put(url, new CachedAiaResponse(certificates));
    }

    /**
     * Verifies that a certificate is currently within its validity period.
     *
     * <p>An expired, or not yet valid, intermediate downloaded via AIA must not be inserted into the chain:
     * embedding it would still fail PKIX path validation at verify time, and silently accepting it would mask the
     * real "this CA certificate needs to be renewed" condition.
     *
     * @param certificate the certificate to check
     * @return true if the certificate is currently valid, false if it is expired or not yet valid
     */
    private static boolean isCurrentlyValid(X509Certificate certificate) {
        try {
            certificate.checkValidity();
            return true;
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            LOGGER.log(FINE, "Issuer certificate [{0}] is expired or not yet valid; rejecting it as an issuer.",
                certificate.getSubjectX500Principal().getName());
            return false;
        }
    }

    /**
     * A cached AIA response. Certificates are held with an expiration time so a reissued or revoked issuer is not
     * served indefinitely.
     */
    private static final class CachedAiaResponse {
        private final List<X509Certificate> certificates;
        private final long expiresAtInMillis;

        private CachedAiaResponse(List<X509Certificate> certificates) {
            this.certificates = certificates;
            this.expiresAtInMillis = System.currentTimeMillis() + AIA_CACHE_TTL_IN_MILLIS;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() >= expiresAtInMillis;
        }
    }
}
