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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.security.auth.x500.X500Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * Utility class used for completing an incomplete certificate chain with the issuer certificates published in the AIA
 * (Authority Information Access) extension of the certificates it already holds.
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
    private static final int AIA_CACHE_MAX_SIZE = 128;
    private static final long MAX_SUCCESS_TTL_IN_MILLIS = TimeUnit.HOURS.toMillis(24);
    private static final long NEGATIVE_TTL_IN_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static final AiaResponseCache AIA_CACHE
        = new AiaResponseCache(AIA_CACHE_MAX_SIZE, System::currentTimeMillis, (message, parameters) -> LOGGER.logp(FINE,
            AiaResponseCache.class.getName(), "diagnostic", message, parameters));

    /**
     * Determines whether a certificate chain should be completed with issuer certificates resolved via AIA.
     *
     * <p>The chain is complete only when its valid, contiguous path from the leaf ends in a self-signed root.
     * A contiguous chain that ends in a non-self-signed intermediate may still be missing one or more issuer
     * certificates above that intermediate. Resolution remains cache-first, so a valid cached response avoids a
     * repeated network request.
     *
     * @param certificates the ordered certificate chain
     * @return true if the valid chain ends in a non-self-signed X.509 certificate, false otherwise
     */
    static boolean shouldCompleteChainViaAia(Certificate[] certificates) {
        if (certificates == null || certificates.length == 0) {
            return false;
        }

        int validChainEnd = findValidChainEnd(Arrays.asList(certificates));
        if (validChainEnd < 0 || !(certificates[validChainEnd] instanceof X509Certificate)) {
            return false;
        }

        return !CertificateUtil.isSelfSignedCertificate((X509Certificate) certificates[validChainEnd]);
    }

    /**
     * Completes an incomplete certificate chain when Authority Information Access (AIA) downloads are allowed for the owning Key Vault client.
     *
     * <p>Because completion may issue outbound HTTP requests on a cache miss, callers must restrict it to chains
     * whose valid path does not end in a self-signed root (see {@link #shouldCompleteChainViaAia(Certificate[])}).
     *
     * <p>The method walks up the contiguous issuer path (leaf → intermediate → root) starting from
     * the first certificate, downloading missing intermediates via AIA. Downloaded issuers are inserted
     * immediately after the current end of the valid chain (before any unplaced/extra certificates).
     * This process repeats until the chain reaches a self-signed root CA, no more AIA URLs are found, or
     * the safety download limit is reached.
     *
     * @param orderedCertificates Certificate array with a contiguous issuer path and any unplaced certificates.
     * @param disableAiaDownload Indicates if AIA certificate downloads should be disabled.
     * @return The original or completed certificate chain.
     */
    static Certificate[] completeChainViaAia(Certificate[] orderedCertificates, boolean disableAiaDownload) {
        if (orderedCertificates == null || orderedCertificates.length == 0) {
            return orderedCertificates;
        }

        if (disableAiaDownload) {
            LOGGER.log(FINE, "AIA chain completion is disabled for this Key Vault client by configuration [{0}]",
                DISABLE_AIA_DOWNLOAD_PROPERTY);
            return orderedCertificates;
        }

        List<Certificate> chain = new ArrayList<>(Arrays.asList(orderedCertificates));
        LOGGER.log(FINE, "Starting AIA chain completion with {0} certificate(s).", chain.size());
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

            // The certificate may come from the response cache, and it may be a root rather than an intermediate,
            // so this message must not claim either.
            LOGGER.log(FINE, "Resolved issuer certificate via AIA: {0}", issuer.getSubjectX500Principal().getName());
            // Insert the downloaded issuer immediately after the valid chain end, before any extra certs
            chain.add(validChainEnd + 1, issuer);
        }

        Certificate[] result = chain.toArray(new Certificate[0]);

        // Log the completed chain for debugging
        if (LOGGER.isLoggable(FINE)) {
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
     * Resolves and validates the issuer, refreshing a cached miss once before briefly suppressing that target.
     *
     * @param cert the certificate whose issuer should be downloaded
     * @return the issuer {@link X509Certificate}, or {@code null} if it cannot be retrieved
     */
    static X509Certificate downloadIssuerCertificateFromAia(X509Certificate cert) {
        try {
            byte[] aiaValue = cert.getExtensionValue(Extension.authorityInfoAccess.getId());
            if (aiaValue == null) {
                LOGGER.log(FINE, "Certificate [{0}] has no AIA extension; issuer download is unavailable.",
                    cert.getSubjectX500Principal().getName());
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

                AiaResponseCache.LookupResult initial = getAiaResponse(url);
                X509Certificate issuer = findValidIssuer(cert, initial.getCertificates());
                if (issuer != null) {
                    return issuer;
                }

                if (initial.isNegative()) {
                    LOGGER.log(FINE, "AIA response for URL [{0}] is empty; no issuer candidate is available for [{1}].",
                        new Object[] { url, cert.getIssuerX500Principal().getName() });
                    continue;
                }

                TargetIdentity targetIdentity
                    = new TargetIdentity(cert.getIssuerX500Principal(), cert.getSerialNumber());
                // A normal load already performed HTTP, so do not download twice in one resolution attempt.
                if (initial.getSource() != AiaResponseCache.Source.CACHE) {
                    LOGGER.log(FINE,
                        "Loaded AIA response for URL [{0}] did not contain a valid issuer for [{1}]; "
                            + "skipping a second download in the same resolution attempt.",
                        new Object[] { url, cert.getIssuerX500Principal().getName() });
                    AIA_CACHE.suppressRefresh(url, targetIdentity, initial.getGeneration(), NEGATIVE_TTL_IN_MILLIS);
                    continue;
                }
                if (AIA_CACHE.isRefreshSuppressed(url, targetIdentity, initial.getGeneration())) {
                    LOGGER.log(FINE,
                        "Skipping forced AIA refresh for URL [{0}], issuer [{1}], generation [{2}] "
                            + "because a recent miss is suppressed.",
                        new Object[] { url, cert.getIssuerX500Principal().getName(), initial.getGeneration() });
                    continue;
                }

                // Refresh only the generation observed above; concurrent callers share the same refresh.
                LOGGER.log(FINE,
                    "Cached AIA response for URL [{0}] did not contain a valid issuer for [{1}]; "
                        + "forcing refresh of generation [{2}].",
                    new Object[] { url, cert.getIssuerX500Principal().getName(), initial.getGeneration() });
                AiaResponseCache.LookupResult refreshed
                    = AIA_CACHE.refreshIfUnchanged(url, initial.getGeneration(), () -> loadAiaResponse(url));
                issuer = findValidIssuer(cert, refreshed.getCertificates());
                if (issuer != null) {
                    AIA_CACHE.clearRefreshSuppression(url, targetIdentity);
                    return issuer;
                }
                LOGGER.log(FINE,
                    "Forced AIA refresh for URL [{0}] did not contain a valid issuer for [{1}]; "
                        + "suppressing another refresh for generation [{2}].",
                    new Object[] { url, cert.getIssuerX500Principal().getName(), refreshed.getGeneration() });
                AIA_CACHE.suppressRefresh(url, targetIdentity, refreshed.getGeneration(), NEGATIVE_TTL_IN_MILLIS);
            }
        } catch (Exception e) {
            LOGGER.log(FINE, "Failed to download issuer certificate from AIA extension.", e);
        }
        return null;
    }

    /**
     * Retrieves a cached AIA response while validating certificate candidates on every use.
     *
     * @param url the CA Issuers URL taken from an AIA extension
     * @return the certificates published at the URL, or an empty list if they cannot be retrieved or parsed
     */
    static List<X509Certificate> fetchCertificatesFromAiaUrl(String url) {
        return getAiaResponse(url).getCertificates();
    }

    /**
     * Gets an AIA response and the cache metadata needed by the refresh decision.
     *
     * @param url the CA Issuers URL
     * @return the cached or loaded response
     */
    private static AiaResponseCache.LookupResult getAiaResponse(String url) {
        return AIA_CACHE.getOrLoadResult(url, () -> loadAiaResponse(url), () -> {
        });
    }

    /**
     * Finds a currently valid candidate that can issue the target certificate.
     *
     * @param target the certificate that needs an issuer
     * @param candidates the certificates published by the AIA endpoint
     * @return a valid issuer, or null when no candidate matches
     */
    private static X509Certificate findValidIssuer(X509Certificate target, List<X509Certificate> candidates) {
        X500Principal expectedIssuerPrincipal = target.getIssuerX500Principal();
        for (X509Certificate candidate : candidates) {
            // Validation runs on every use, including cache hits, so a cached certificate can never shortcut
            // subject, validity or issuer verification.
            if (expectedIssuerPrincipal.equals(candidate.getSubjectX500Principal())
                && isCurrentlyValid(candidate)
                && CertificateUtil.isValidIssuer(candidate, target)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Downloads and parses one AIA response into a cache entry.
     *
     * @param url the CA Issuers URL
     * @return a positive or negative cache entry
     */
    private static AiaResponseCache.Entry loadAiaResponse(String url) {
        LOGGER.log(FINE, "Downloading issuer certificate from AIA URL: {0}", url);
        long now = System.currentTimeMillis();
        HttpUtil.BinaryHttpResponse response = HttpUtil.getBytesWithMetadata(url);
        byte[] certBytes = response.getBody();
        if (certBytes == null) {
            LOGGER.log(FINE, "Failed to download issuer certificate from AIA URL: {0}", url);
            return new AiaResponseCache.Entry(Collections.emptyList(), calculateNegativeExpiration(response, now));
        }

        List<X509Certificate> certificates = parseCertificates(certBytes);
        if (certificates.isEmpty()) {
            LOGGER.log(FINE, "AIA response from URL [{0}] did not contain a parsable certificate.", url);
            return new AiaResponseCache.Entry(certificates, calculateNegativeExpiration(response, now));
        }

        LOGGER.log(FINE, "Loaded {0} certificate(s) from AIA URL: {1}", new Object[] { certificates.size(), url });
        return new AiaResponseCache.Entry(certificates, calculateResponseExpiration(response, now));
    }

    /**
     * Calculates how long an HTTP response may remain cached.
     *
     * @param response the HTTP response and its freshness headers
     * @param nowInMillis the current time in epoch milliseconds
     * @return the expiration time in epoch milliseconds
     */
    static long calculateResponseExpiration(HttpUtil.BinaryHttpResponse response, long nowInMillis) {
        String cacheControl = response.getCacheControl();
        if (hasCacheDirective(cacheControl, "no-store") || hasCacheDirective(cacheControl, "no-cache")) {
            return nowInMillis;
        }

        long expiresAt = safeAdd(nowInMillis, MAX_SUCCESS_TTL_IN_MILLIS);
        Long maxAgeInSeconds = getCacheDirectiveSeconds(cacheControl, "max-age");
        long ageInSeconds = parseNonNegativeLong(response.getAge(), 0L);
        Long dateHeader = parseHttpDate(response.getDate());
        long apparentAgeInMillis = dateHeader == null ? 0L : Math.max(0L, nowInMillis - dateHeader);
        long currentAgeInMillis = Math.max(apparentAgeInMillis, secondsToMillis(ageInSeconds));
        if (maxAgeInSeconds != null) {
            long freshnessLifetime = secondsToMillis(maxAgeInSeconds);
            long remaining = Math.max(0L, freshnessLifetime - currentAgeInMillis);
            expiresAt = Math.min(expiresAt, safeAdd(nowInMillis, remaining));
        } else {
            Long expiresHeader = parseHttpDate(response.getExpires());
            if (expiresHeader != null) {
                long freshnessLifetime = Math.max(0L, expiresHeader - (dateHeader == null ? nowInMillis : dateHeader));
                long remaining = Math.max(0L, freshnessLifetime - currentAgeInMillis);
                expiresAt = Math.min(expiresAt, safeAdd(nowInMillis, remaining));
            }
        }

        return expiresAt;
    }

    /**
     * Calculates the short cache period for a failed or empty response.
     *
     * @param response the HTTP response and its cache directives
     * @param nowInMillis the current time in epoch milliseconds
     * @return the expiration time in epoch milliseconds
     */
    private static long calculateNegativeExpiration(HttpUtil.BinaryHttpResponse response, long nowInMillis) {
        String cacheControl = response.getCacheControl();
        return hasCacheDirective(cacheControl, "no-store") || hasCacheDirective(cacheControl, "no-cache")
            ? nowInMillis
            : safeAdd(nowInMillis, NEGATIVE_TTL_IN_MILLIS);
    }

    private static boolean hasCacheDirective(String cacheControl, String expectedDirective) {
        if (cacheControl == null) {
            return false;
        }
        for (String directive : cacheControl.split(",")) {
            String normalized = directive.trim().toLowerCase(Locale.ROOT);
            if (normalized.equals(expectedDirective) || normalized.startsWith(expectedDirective + "=")) {
                return true;
            }
        }
        return false;
    }

    private static Long getCacheDirectiveSeconds(String cacheControl, String expectedDirective) {
        if (cacheControl == null) {
            return null;
        }
        for (String directive : cacheControl.split(",")) {
            String[] parts = directive.trim().split("=", 2);
            if (parts.length == 2 && expectedDirective.equalsIgnoreCase(parts[0].trim())) {
                String value = parts[1].trim();
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                long parsed = parseNonNegativeLong(value, -1L);
                return parsed < 0 ? null : parsed;
            }
        }
        return null;
    }

    private static long parseNonNegativeLong(String value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return defaultValue;
        }
        for (int i = 0; i < normalized.length(); i++) {
            if (!Character.isDigit(normalized.charAt(i))) {
                return defaultValue;
            }
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    private static Long parseHttpDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static long secondsToMillis(long seconds) {
        return seconds > Long.MAX_VALUE / 1000L ? Long.MAX_VALUE : seconds * 1000L;
    }

    private static long safeAdd(long value, long increment) {
        return increment > Long.MAX_VALUE - value ? Long.MAX_VALUE : value + increment;
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
     * Identifies the target certificate for refresh suppression.
     *
     * <p>The issuer principal and serial number distinguish certificates that use the same AIA URL.
     */
    private static final class TargetIdentity {
        private final X500Principal issuerPrincipal;
        private final BigInteger serialNumber;

        private TargetIdentity(X500Principal issuerPrincipal, BigInteger serialNumber) {
            this.issuerPrincipal = issuerPrincipal;
            this.serialNumber = serialNumber;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TargetIdentity)) {
                return false;
            }
            TargetIdentity other = (TargetIdentity) obj;
            return issuerPrincipal.equals(other.issuerPrincipal) && serialNumber.equals(other.serialNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(issuerPrincipal, serialNumber);
        }
    }

}
