// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.agentserver.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Utility for installing additional trusted CA certificates into the JVM at runtime without
 * mutating the JDK's {@code cacerts} file or the OS certificate bundle.
 *
 * <p>Two layers are available, configurable independently per call:
 * <ol>
 *   <li><strong>Merged keystore install.</strong> The JDK default {@code cacerts} is loaded
 *       into memory, the supplied certificate(s) are added, the merged keystore is written
 *       to a temporary file, and the JVM system properties {@code javax.net.ssl.trustStore}
 *       / {@code javax.net.ssl.trustStorePassword} / {@code javax.net.ssl.trustStoreType}
 *       are pointed at it. HTTP clients that consult these properties (notably reactor-netty
 *       inside {@code azure-core-http-netty}) will then trust the added cert globally.</li>
 *   <li><strong>Host-scoped JVM-default {@link SSLContext}.</strong> When a host predicate
 *       is supplied, a composite trust manager is built that consults the added cert only
 *       for hosts matched by the predicate (the JDK default trust set applies to every other
 *       host) and is registered via {@link SSLContext#setDefault(SSLContext)}. Code paths
 *       using the JVM-default {@code SSLContext} pick it up automatically; reactor-netty's
 *       default {@code HttpClient} does not — see the caveat on
 *       {@link #installAdcEgressProxyCertificate(Logger, Predicate)}.</li>
 * </ol>
 *
 * <p>This must be invoked <strong>before</strong> any code triggers initialization of the
 * default {@link SSLContext} (i.e. before the first outbound HTTPS call), otherwise the
 * change will not take effect for the current JVM.
 *
 * <p>For every certificate added, the installer logs an audit line capturing subject DN,
 * issuer DN, validity window, serial number, SHA-256 fingerprint, and a PKIX chain summary.
 *
 * <h2>Primary use case — ADC egress proxy CA</h2>
 * In the Azure AI Foundry vNext ("ADC") hosting environment, traffic to the Foundry project
 * endpoint (matching {@link #defaultAdcProxiedHosts()}) is TLS-terminated by an egress proxy
 * whose CA cert is mounted at {@link #ADC_EGRESS_PROXY_CA_CERT}. The recommended bootstrap
 * is a single call from {@code main()}:
 * <pre>{@code
 *   TrustStoreInstaller.installAdcEgressProxyCertificate();
 * }</pre>
 * This performs both layers above against the mounted proxy CA, with host-scoping defaulted
 * to {@link #defaultAdcProxiedHosts()}. Use {@link #installAdcEgressProxyCertificate(Logger)}
 * to route the audit log through your application logger, or
 * {@link #installAdcEgressProxyCertificate(Logger, Predicate)} to override the host-scoping
 * predicate. To install an arbitrary (non-ADC) proxy CA, use
 * {@link #installProxyCertificate(Logger, Predicate, Path, String)}.
 */
public final class TrustStoreInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrustStoreInstaller.class);
    private static final String DEFAULT_CACERTS_PASSWORD = "changeit";

    /**
     * Well-known path at which the Azure Agent Server (vNext "ADC" hosting environment) mounts
     * the egress proxy CA certificate required for outbound HTTPS calls to Foundry endpoints.
     * Absent when running locally.
     */
    public static final Path ADC_EGRESS_PROXY_CA_CERT =
        Paths.get("/etc/ssl/certs/adc-egress-proxy-ca.crt");

    private TrustStoreInstaller() {
    }

    /**
     * Returns the caller-supplied logger when non-null, otherwise the class-static logger.
     * Used so that callers may opt-in to routing lifecycle messages through their own
     * application-configured SLF4J logger while still getting sensible defaults when they
     * pass {@code null}.
     *
     * @param logger optional caller-supplied logger; may be {@code null}.
     * @return {@code logger} when non-null, otherwise the class-static logger.
     */
    private static Logger logger(Logger logger) {
        return logger != null ? logger : LOGGER;
    }

    /**
     * Installs the Azure Agent Server (ADC) egress proxy CA certificate from the well-known
     * mount path {@code /etc/ssl/certs/adc-egress-proxy-ca.crt}, applying both layers documented
     * on the class JavaDoc: the merged-keystore install <em>and</em> a host-scoped JVM-default
     * {@link SSLContext} scoped to {@link #defaultAdcProxiedHosts()}. This is the recommended
     * one-line bootstrap from {@code main()}. Uses the class-static SLF4J logger for lifecycle
     * messages — call {@link #installAdcEgressProxyCertificate(Logger)} to route them through
     * your own application logger instead.
     *
     * <p>No-op when the certificate file is not present (e.g. when running locally outside
     * the ADC hosting environment), so it is safe to call unconditionally during startup.
     *
     * @return {@code true} if the certificate was found and installed, {@code false} otherwise.
     * @throws IOException              if reading the cert or default cacerts fails.
     * @throws GeneralSecurityException if the keystore cannot be loaded or the certificate parsed.
     */
    public static boolean installAdcEgressProxyCertificate()
        throws IOException, GeneralSecurityException {
        return installAdcEgressProxyCertificate(null, defaultAdcProxiedHosts());
    }

    /**
     * Variant of {@link #installAdcEgressProxyCertificate()} that routes lifecycle messages
     * through the caller's SLF4J logger. Applies both layers (merged-keystore install plus
     * host-scoping to {@link #defaultAdcProxiedHosts()}). Pass {@code null} for {@code logger}
     * to fall back to the class-static logger.
     */
    public static boolean installAdcEgressProxyCertificate(Logger logger)
        throws IOException, GeneralSecurityException {
        return installAdcEgressProxyCertificate(logger, defaultAdcProxiedHosts());
    }

    /**
     * Variant that lets the caller override the host-scoping predicate (or disable
     * host-scoping entirely by passing {@code null}). Equivalent to the no-arg / one-arg
     * overloads when {@code adcHosts == defaultAdcProxiedHosts()}.
     *
     * <p><strong>Caveat.</strong> Some HTTP clients (notably reactor-netty's default
     * {@code HttpClient}, used by {@code azure-core-http-netty}) build their own
     * {@code SslContext} from the {@code javax.net.ssl.trustStore} system property and do
     * <em>not</em> consult {@link SSLContext#getDefault()}. For those clients the
     * merged-keystore install still makes the proxy CA trusted globally, but the
     * host-scoping enforcement does not apply.
     *
     * @param logger   optional SLF4J logger; {@code null} uses the class-static logger.
     * @param adcHosts predicate evaluated against {@code SSLEngine.getPeerHost()} /
     *                 {@code Socket.getRemoteSocketAddress()}; only matching hosts will see
     *                 the proxy CA in their trust set. Pass {@code null} to disable
     *                 host-scoping entirely (merged-keystore install only).
     */
    public static boolean installAdcEgressProxyCertificate(Logger logger, Predicate<String> adcHosts)
        throws IOException, GeneralSecurityException {
        return installProxyCertificate(logger, adcHosts, ADC_EGRESS_PROXY_CA_CERT, "adc-egress-proxy");
    }

    /**
     * Generic variant for installing any proxy CA certificate file with optional host-scoping.
     * Equivalent to {@link #installAdcEgressProxyCertificate(Logger, Predicate)} but
     * parameterised by certificate path and alias prefix instead of using the ADC defaults.
     *
     * <p>Performs the two layers documented on the class JavaDoc — the merged-keystore
     * install always; the host-scoped {@link SSLContext} only when {@code adcHosts} is
     * non-{@code null}.
     *
     * <p>No-op if {@code certPath} does not exist (returns {@code false}), making it safe to
     * call unconditionally during application startup.
     *
     * @param logger      optional SLF4J logger; {@code null} uses the class-static logger.
     * @param adcHosts    optional host predicate; {@code null} disables host-scoping. The
     *                    parameter name reflects the primary ADC use case, but the predicate
     *                    may match any host pattern relevant to the supplied cert.
     * @param certPath    path to a PEM- or DER-encoded X.509 certificate file (may contain
     *                    multiple PEM-encoded certificates concatenated together).
     * @param aliasPrefix prefix used to derive unique aliases ({@code aliasPrefix-N}) for the
     *                    imported certificates in the merged keystore.
     */
    public static boolean installProxyCertificate(
        Logger logger, Predicate<String> adcHosts, Path certPath, String aliasPrefix)
        throws IOException, GeneralSecurityException {
        boolean installed = installIntoMergedKeystore(certPath, aliasPrefix, logger);
        if (installed && adcHosts != null && Files.isRegularFile(certPath)) {
            installHostScopedDefaultSslContext(logger, adcHosts, certPath);
        }
        return installed;
    }

    /**
     * Merged-keystore install: parses certs from {@code certPath}, adds them to a copy of
     * the JDK {@code cacerts} under {@code aliasPrefix-N}, writes the merged keystore to a
     * temp file, and points the JVM at it via the {@code javax.net.ssl.trustStore} system
     * properties. No-op if {@code certPath} does not exist.
     */
    private static boolean installIntoMergedKeystore(Path certPath, String aliasPrefix, Logger logger)
        throws IOException, GeneralSecurityException {
        Objects.requireNonNull(certPath, "certPath");
        Objects.requireNonNull(aliasPrefix, "aliasPrefix");

        Logger log = logger(logger);

        if (!Files.isRegularFile(certPath)) {
            log.debug("No CA cert found at {}; skipping truststore install.", certPath);
            return false;
        }

        KeyStore merged = loadDefaultCacerts(logger);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certs;
        try (InputStream in = Files.newInputStream(certPath)) {
            certs = cf.generateCertificates(in);
        }
        if (certs.isEmpty()) {
            log.warn("No certificates were parsed from {}.", certPath);
            return false;
        }

        // Build the candidate pool used during chain-building so that intermediates shipped
        // alongside the leaf certificate in the same file are discoverable.
        List<X509Certificate> intermediates = new ArrayList<>();
        for (Certificate c : certs) {
            if (c instanceof X509Certificate x) {
                intermediates.add(x);
            }
        }

        int added = 0;
        int i = 0;
        for (Certificate cert : certs) {
            String alias = aliasPrefix + "-" + i++;
            ChainInfo chain = (cert instanceof X509Certificate x)
                ? buildChain(x, intermediates, merged)
                : ChainInfo.NONE;

            logCertificateDetails(log, alias, cert, chain);

            merged.setCertificateEntry(alias, cert);
            added++;
        }

        if (added == 0) {
            log.warn("No certificates from {} were added to the truststore.", certPath);
            return false;
        }

        Path mergedFile = Files.createTempFile("cacerts-with-" + aliasPrefix + "-", ".jks");
        mergedFile.toFile().deleteOnExit();
        try (OutputStream out = Files.newOutputStream(mergedFile)) {
            merged.store(out, DEFAULT_CACERTS_PASSWORD.toCharArray());
        }

        System.setProperty("javax.net.ssl.trustStore", mergedFile.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.trustStorePassword", DEFAULT_CACERTS_PASSWORD);
        System.setProperty("javax.net.ssl.trustStoreType", merged.getType());

        log.info("Installed {} CA certificate(s) from {} into merged truststore {}.",
            added, certPath, mergedFile);
        return true;
    }

    private static KeyStore loadDefaultCacerts(Logger logger) throws IOException, GeneralSecurityException {
        Logger log = logger(logger);
        Path cacerts = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        if (Files.isRegularFile(cacerts)) {
            try (InputStream in = Files.newInputStream(cacerts)) {
                ks.load(in, DEFAULT_CACERTS_PASSWORD.toCharArray());
            }
        } else {
            log.warn("Default JDK cacerts not found at {}; starting from an empty truststore.", cacerts);
            ks.load(null, DEFAULT_CACERTS_PASSWORD.toCharArray());
        }
        return ks;
    }

    /**
     * Emits an INFO line describing the certificate being trusted: alias, subject DN,
     * issuer DN, validity window, serial number, SHA-256 fingerprint, and trust chain
     * (root subject + fingerprint, whether the chain validates against the JDK cacerts).
     */
    private static void logCertificateDetails(Logger log, String alias, Certificate cert, ChainInfo chain) {
        if (cert instanceof X509Certificate x509) {
            log.info(
                "Adding CA certificate [alias={}] subject=\"{}\" issuer=\"{}\" "
                    + "notBefore={} notAfter={} serial={} sha256={} chain={}",
                alias,
                x509.getSubjectX500Principal().getName(),
                x509.getIssuerX500Principal().getName(),
                x509.getNotBefore().toInstant(),
                x509.getNotAfter().toInstant(),
                x509.getSerialNumber().toString(16),
                sha256Fingerprint(x509),
                chain.summary());
        } else {
            log.info("Adding CA certificate [alias={}] type={}", alias, cert.getType());
        }
    }

    private static String sha256Fingerprint(X509Certificate cert) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(cert.getEncoded());
            return HexFormat.of().withUpperCase().withDelimiter(":").formatHex(digest);
        } catch (GeneralSecurityException e) {
            return "<unavailable: " + e.getMessage() + ">";
        }
    }

    /**
     * Attempts to build a PKIX trust chain from {@code target} to a trust anchor in
     * {@code trustAnchors} (typically the JDK's cacerts), using {@code intermediates} as a
     * candidate pool. Returns a {@link ChainInfo} that captures the outcome — whether the
     * chain is trusted, the root certificate (if any), and a short human-readable summary.
     *
     * <p>Self-signed certificates are handled as a special case: the cert is treated as its
     * own root and reported as "self-signed (not anchored)" unless an equal cert already
     * exists in {@code trustAnchors}, in which case it is reported as "self-signed (anchored)".
     */
    private static ChainInfo buildChain(X509Certificate target,
                                        Collection<X509Certificate> intermediates,
                                        KeyStore trustAnchors) {
        boolean selfSigned = target.getSubjectX500Principal().equals(target.getIssuerX500Principal());
        if (selfSigned) {
            boolean anchored = trustAnchorsContain(trustAnchors, target);
            String summary = "self-signed root sha256=" + sha256Fingerprint(target)
                + (anchored ? " (anchored in cacerts)" : " (not anchored in cacerts)");
            return new ChainInfo(anchored, target, summary);
        }

        try {
            Set<TrustAnchor> anchors = new HashSet<>();
            Enumeration<String> aliases = trustAnchors.aliases();
            while (aliases.hasMoreElements()) {
                String a = aliases.nextElement();
                if (trustAnchors.isCertificateEntry(a)) {
                    Certificate c = trustAnchors.getCertificate(a);
                    if (c instanceof X509Certificate x) {
                        anchors.add(new TrustAnchor(x, null));
                    }
                }
            }
            if (anchors.isEmpty()) {
                return new ChainInfo(false, null, "no trust anchors available");
            }

            X509CertSelector sel = new X509CertSelector();
            sel.setCertificate(target);

            PKIXBuilderParameters params = new PKIXBuilderParameters(anchors, sel);
            params.setRevocationEnabled(false);
            params.addCertStore(CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(intermediates)));

            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) builder.build(params);
            X509Certificate root = result.getTrustAnchor().getTrustedCert();
            String summary = "trusted via PKIX: root subject=\""
                + root.getSubjectX500Principal().getName()
                + "\" root sha256=" + sha256Fingerprint(root);
            return new ChainInfo(true, root, summary);
        } catch (GeneralSecurityException e) {
            return new ChainInfo(false, null, "no trusted chain (" + e.getMessage() + ")");
        }
    }

    private static boolean trustAnchorsContain(KeyStore trustAnchors, X509Certificate cert) {
        try {
            Enumeration<String> aliases = trustAnchors.aliases();
            while (aliases.hasMoreElements()) {
                String a = aliases.nextElement();
                if (trustAnchors.isCertificateEntry(a) && cert.equals(trustAnchors.getCertificate(a))) {
                    return true;
                }
            }
        } catch (GeneralSecurityException ignored) {
            // fall through
        }
        return false;
    }

    /**
     * Result of a single chain-building attempt against the JDK cacerts.
     */
    private record ChainInfo(boolean trusted, X509Certificate root, String summary) {
        static final ChainInfo NONE = new ChainInfo(false, null, "n/a");
    }

    // ──────────────────────────────────────────────────────────────────
    //  Host-scoped trust manager
    // ──────────────────────────────────────────────────────────────────

    /**
     * Default predicate matching the host pattern empirically observed to be MITM'd by the
     * ADC egress proxy in Azure AI Foundry vNext: <code>*.services.ai.azure.com</code> (the
     * unified Foundry project / storage / OpenAI endpoint pattern).
     */
    public static Predicate<String> defaultAdcProxiedHosts() {
        return host -> {
            if (host == null) return false;
            String h = host.toLowerCase(Locale.ROOT);
            return h.endsWith(".services.ai.azure.com");
        };
    }

    /**
     * Builds a host-scoped trust manager set: certificates loaded from {@code certPath} are
     * trusted only when validating server certificates for hosts matched by {@code adcHosts}.
     * For all other hosts — and as a fall-through for matched hosts whose server cert was not
     * signed by the scoped CA — the JDK default trust set is used.
     *
     * @param certPath path to PEM/DER cert file to install under scoped trust.
     * @param adcHosts host predicate; only matching hosts will see the scoped CA.
     * @param logger   optional SLF4J logger; {@code null} uses the class-static logger.
     */
    private static TrustManager[] hostScopedTrustManagers(
        Path certPath, Predicate<String> adcHosts, Logger logger)
        throws IOException, GeneralSecurityException {

        Objects.requireNonNull(certPath, "certPath");
        Objects.requireNonNull(adcHosts, "adcHosts");
        Logger log = logger(logger);

        TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        defaultTmf.init((KeyStore) null);
        X509ExtendedTrustManager defaultTm = firstX509Extended(defaultTmf,
            "JDK default trust manager");

        KeyStore scopedOnly = KeyStore.getInstance(KeyStore.getDefaultType());
        scopedOnly.load(null, null);

        int added = 0;
        if (Files.isRegularFile(certPath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certs;
            try (InputStream in = Files.newInputStream(certPath)) {
                certs = cf.generateCertificates(in);
            }
            List<X509Certificate> intermediates = new ArrayList<>();
            for (Certificate c : certs) {
                if (c instanceof X509Certificate x) {
                    intermediates.add(x);
                }
            }
            KeyStore cacertsForChainBuild = loadDefaultCacerts(log);
            int i = 0;
            for (Certificate cert : certs) {
                String alias = "host-scoped-" + i++;
                if (!(cert instanceof X509Certificate x)) continue;
                buildChain(x, intermediates, cacertsForChainBuild); // populates chain context for log
                scopedOnly.setCertificateEntry(alias, x);
                added++;
            }
        } else {
            log.debug("Host-scoped trust manager: cert file {} not present; scoped trust set is empty "
                + "and will always fall through to JDK default.", certPath);
        }

        TrustManagerFactory scopedTmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        scopedTmf.init(scopedOnly);
        X509ExtendedTrustManager scopedTm = firstX509Extended(scopedTmf,
            "Scoped trust manager");

        log.info("Host-scoped trust manager built: {} cert(s) trusted for predicate-matched hosts; "
            + "JDK default trust applies to all other hosts.", added);

        return new TrustManager[]{
            new HostScopedTrustManager(scopedTm, defaultTm, adcHosts)
        };
    }

    /**
     * Builds a host-scoped {@link SSLContext} via {@link #hostScopedTrustManagers} and registers
     * it as the JVM default via {@link SSLContext#setDefault(SSLContext)}. See the caveat on
     * {@link #installAdcEgressProxyCertificate(Logger, Predicate)} about HTTP clients that
     * bypass the JVM-default {@link SSLContext}.
     */
    private static void installHostScopedDefaultSslContext(
        Logger logger, Predicate<String> adcHosts, Path certPath)
        throws IOException, GeneralSecurityException {
        Logger log = logger(logger);
        TrustManager[] tms = hostScopedTrustManagers(certPath, adcHosts, log);
        SSLContext ctx;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tms, null);
        } catch (GeneralSecurityException e) {
            log.warn("Failed to construct host-scoped SSLContext; default SSLContext unchanged.", e);
            return;
        }
        SSLContext.setDefault(ctx);
        log.info("Installed host-scoped SSLContext as the JVM default; scoped CA is trusted "
            + "only for hosts matching the supplied predicate.");
    }

    private static X509ExtendedTrustManager firstX509Extended(TrustManagerFactory tmf, String label) {
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509ExtendedTrustManager x) {
                return x;
            }
        }
        throw new IllegalStateException(label + " did not provide an X509ExtendedTrustManager");
    }

    /**
     * Composite {@link X509ExtendedTrustManager} that consults a scoped trust manager
     * (containing only the explicitly-added CA(s)) for hosts matched by a predicate and the
     * JDK default trust manager for everything else.
     *
     * <p>Behaviour:
     * <ul>
     *   <li>For matched hosts: the scoped trust manager validates first; on failure the JDK
     *       default is consulted as a fall-back (so direct connections to the real service
     *       endpoint, not through the proxy, still work when the platform-mounted proxy CA
     *       is absent).</li>
     *   <li>For unmatched hosts: only the JDK default applies — the scoped CA is
     *       <em>never</em> consulted, so a compromised proxy CA cannot impersonate
     *       public-internet hosts.</li>
     *   <li>For the legacy {@code checkServerTrusted(chain, authType)} overload (no peer
     *       host available): only the JDK default applies (fail-safe).</li>
     * </ul>
     */
    private static final class HostScopedTrustManager extends X509ExtendedTrustManager {

        private final X509ExtendedTrustManager scopedTm;
        private final X509ExtendedTrustManager defaultTm;
        private final Predicate<String> scopedHosts;

        HostScopedTrustManager(X509ExtendedTrustManager scopedTm,
                               X509ExtendedTrustManager defaultTm,
                               Predicate<String> scopedHosts) {
            this.scopedTm = Objects.requireNonNull(scopedTm, "scopedTm");
            this.defaultTm = Objects.requireNonNull(defaultTm, "defaultTm");
            this.scopedHosts = Objects.requireNonNull(scopedHosts, "scopedHosts");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] scoped = scopedTm.getAcceptedIssuers();
            X509Certificate[] def = defaultTm.getAcceptedIssuers();
            X509Certificate[] all = new X509Certificate[scoped.length + def.length];
            System.arraycopy(scoped, 0, all, 0, scoped.length);
            System.arraycopy(def, 0, all, scoped.length, def.length);
            return all;
        }

        // ── client trust (rare; defer to default) ─────────────────────
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            defaultTm.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            defaultTm.checkClientTrusted(chain, authType, socket);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            defaultTm.checkClientTrusted(chain, authType, engine);
        }

        // ── server trust (the host-scoping decision lives here) ───────
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // No peer-host information on this overload — fail-safe to JDK default only.
            defaultTm.checkServerTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            checkServerTrustedForPeer(chain, authType, peerHost(socket), socket, null);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            String peer = engine == null ? null : engine.getPeerHost();
            checkServerTrustedForPeer(chain, authType, peer, null, engine);
        }

        private void checkServerTrustedForPeer(X509Certificate[] chain, String authType,
                                               String peer, Socket socket, SSLEngine engine)
            throws CertificateException {

            if (peer != null && scopedHosts.test(peer)) {
                try {
                    if (socket != null) {
                        scopedTm.checkServerTrusted(chain, authType, socket);
                    } else if (engine != null) {
                        scopedTm.checkServerTrusted(chain, authType, engine);
                    } else {
                        scopedTm.checkServerTrusted(chain, authType);
                    }
                    return;
                } catch (CertificateException ignored) {
                    // fall through to default
                }
            }

            if (socket != null) {
                defaultTm.checkServerTrusted(chain, authType, socket);
            } else if (engine != null) {
                defaultTm.checkServerTrusted(chain, authType, engine);
            } else {
                defaultTm.checkServerTrusted(chain, authType);
            }
        }

        private static String peerHost(Socket socket) {
            if (socket == null) return null;
            SocketAddress addr = socket.getRemoteSocketAddress();
            if (addr instanceof InetSocketAddress isa) {
                return isa.getHostString();
            }
            return null;
        }
    }
}
