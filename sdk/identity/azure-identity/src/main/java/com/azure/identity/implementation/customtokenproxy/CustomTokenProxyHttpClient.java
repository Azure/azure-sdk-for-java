// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.SniSslSocketFactory;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class CustomTokenProxyHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(CustomTokenProxyHttpClient.class);

    private SSLContext cachedSSLContext;
    private byte[] cachedCaData;
    private SSLSocketFactory cachedSslSocketFactory;
    private final URL proxyUrl;
    private final String sniName;
    private final byte[] caData;
    private final String caFile;

    public CustomTokenProxyHttpClient(ProxyConfig proxyConfig) {
        this.proxyUrl = proxyConfig.getTokenProxyUrl();
        this.sniName = proxyConfig.getSniName();
        this.caData = proxyConfig.getCaData();
        this.caFile = proxyConfig.getCaFile();
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return Mono.fromCallable(() -> sendSync(request, context))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnDiscard(HttpResponse.class, HttpResponse::close);
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        HttpURLConnection connection = null;
        HttpResponse response = null;
        try {
            HttpRequest proxyRequest = request.copy().setUrl(rewriteTokenRequestForProxy(request.getUrl()));
            connection = createConnection(proxyRequest);
            response = new CustomTokenProxyHttpResponse(proxyRequest, connection);
            return response;
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to create connection to token proxy", e));
        } finally {
            if (response == null && connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection createConnection(HttpRequest request) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) request.getUrl().openConnection();
        boolean requestSent = false;
        try {
            connection.setSSLSocketFactory(getSSLSocketFactory());
            if (!CoreUtils.isNullOrEmpty(sniName)) {
                connection.setHostnameVerifier(sniAwareVerifier(sniName, proxyUrl));
            }

            connection.setRequestMethod(request.getHttpMethod().toString());
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(20_000);

            BinaryData bodyData = request.getBodyAsBinaryData();
            connection.setDoOutput(bodyData != null);

            request.getHeaders().forEach(header -> connection.addRequestProperty(header.getName(), header.getValue()));

            connection.connect();
            // HttpsURLConnection can skip its verifier when the certificate matches the URL host.
            if (!CoreUtils.isNullOrEmpty(sniName)
                && !certificateMatchesServerName(connection.getServerCertificates(), sniName)) {
                throw LOGGER.logThrowableAsError(new SSLPeerUnverifiedException(
                    "Token proxy certificate does not match AZURE_KUBERNETES_SNI_NAME."));
            }

            if (bodyData != null) {
                byte[] bytes = bodyData.toBytes();
                if (bytes.length > 0) {
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(bytes);
                    }
                }
            }
            requestSent = true;
            return connection;
        } finally {
            if (!requestSent) {
                connection.disconnect();
            }
        }
    }

    private URL rewriteTokenRequestForProxy(URL originalUrl) throws MalformedURLException {
        String tokenProxyBase = proxyUrl.toString();
        if (!tokenProxyBase.endsWith("/")) {
            tokenProxyBase += "/";
        }
        String originalPath = originalUrl.getPath();
        String combined = tokenProxyBase + (originalPath.startsWith("/") ? originalPath.substring(1) : originalPath);
        if (originalUrl.getQuery() != null) {
            combined += "?" + originalUrl.getQuery();
        }
        return new URL(combined);
    }

    private synchronized SSLSocketFactory getSSLSocketFactory() {
        SSLContext sslContext = getSSLContext();
        if (cachedSslSocketFactory == null) {
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            if (!CoreUtils.isNullOrEmpty(sniName)) {
                sslSocketFactory = new SniSslSocketFactory(sslSocketFactory, sniName);
            }
            cachedSslSocketFactory = sslSocketFactory;
        }
        return cachedSslSocketFactory;
    }

    private SSLContext getSSLContext() {
        try {
            if (CoreUtils.isNullOrEmpty(caFile)) {
                if (cachedSSLContext == null) {
                    if (caData != null && caData.length > 0) {
                        cachedSSLContext = createSslContextFromBytes(caData);
                    } else {
                        cachedSSLContext
                            = CoreUtils.isNullOrEmpty(sniName) ? SSLContext.getDefault() : initializeSslContext(null);
                    }
                }
                return cachedSSLContext;
            }

            byte[] currentContent;
            try {
                currentContent = Files.readAllBytes(Paths.get(caFile));
            } catch (NoSuchFileException e) {
                if (cachedSSLContext == null) {
                    throw LOGGER.logExceptionAsError(
                        new IllegalStateException("Token proxy CA file is missing before its first load.", e));
                }
                LOGGER.warning("Token proxy CA file is temporarily missing; retaining the previously loaded CA.");
                return cachedSSLContext;
            }

            if (currentContent.length == 0) {
                if (cachedSSLContext == null) {
                    throw LOGGER.logExceptionAsError(new IllegalStateException("Token proxy CA file is empty."));
                }
                LOGGER.warning("Token proxy CA file is temporarily empty; retaining the previously loaded CA.");
                return cachedSSLContext;
            }

            if (!Arrays.equals(currentContent, cachedCaData)) {
                cachedSSLContext = createSslContextFromBytes(currentContent);
                cachedCaData = currentContent;
                cachedSslSocketFactory = null;
            }

            return cachedSSLContext;

        } catch (IOException | GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Failed to initialize TLS trust for the token proxy.", e));
        }
    }

    private SSLContext createSslContextFromBytes(byte[] certificateData) throws GeneralSecurityException, IOException {
        try (InputStream inputStream = new ByteArrayInputStream(certificateData)) {
            Collection<? extends Certificate> certificates
                = CertificateFactory.getInstance("X.509").generateCertificates(inputStream);

            if (certificates.isEmpty()) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("No valid certificates found"));
            }
            return createSslContext(certificates);
        }
    }

    private SSLContext createSslContext(Collection<? extends Certificate> certificates)
        throws GeneralSecurityException, IOException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, null);
        int index = 1;
        for (Certificate caCert : certificates) {
            keystore.setCertificateEntry("ca-cert-" + index++, caCert);
        }
        return initializeSslContext(keystore);
    }

    private SSLContext initializeSslContext(KeyStore keystore) throws GeneralSecurityException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keystore);
        TrustManager[] trustManagers = tmf.getTrustManagers();
        if (!CoreUtils.isNullOrEmpty(sniName)) {
            for (int i = 0; i < trustManagers.length; i++) {
                if (trustManagers[i] instanceof X509ExtendedTrustManager) {
                    trustManagers[i]
                        = new SniExtendedTrustManager((X509ExtendedTrustManager) trustManagers[i], sniName);
                } else if (trustManagers[i] instanceof X509TrustManager) {
                    trustManagers[i] = new SniTrustManager((X509TrustManager) trustManagers[i], sniName);
                }
            }
        }

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagers, null);
        return context;
    }

    private static HostnameVerifier sniAwareVerifier(String sniName, URL customProxyUrl) {
        return (urlHost, session) -> {
            if (!customProxyUrl.getHost().equalsIgnoreCase(session.getPeerHost())) {
                return false;
            }
            try {
                return certificateMatchesServerName(session.getPeerCertificates(), sniName);
            } catch (SSLPeerUnverifiedException e) {
                return false;
            }
        };
    }

    private static boolean certificateMatchesServerName(Certificate[] certificates, String serverName) {
        return certificates.length > 0
            && certificates[0] instanceof X509Certificate
            && certificateContainsDnsName((X509Certificate) certificates[0], serverName);
    }

    private static boolean certificateContainsDnsName(X509Certificate cert, String dnsName) {
        try {
            Collection<List<?>> sanList = cert.getSubjectAlternativeNames();
            if (sanList != null) {
                for (List<?> san : sanList) {
                    if (san.size() >= 2 && san.get(0).equals(2)) {
                        String certDnsName = (String) san.get(1);
                        if (matchesDnsName(dnsName, certDnsName)) {
                            return true;
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {
            return false;
        }
        return false;
    }

    static boolean matchesDnsName(String dnsName, String certificateDnsName) {
        if (certificateDnsName.indexOf('*') == -1) {
            return certificateDnsName.equalsIgnoreCase(dnsName);
        }

        // A certificate wildcard can replace only the complete left-most DNS label (RFC 9525, section 6.3).
        if (!certificateDnsName.startsWith("*.")
            || certificateDnsName.length() == 2
            || certificateDnsName.indexOf('*', 1) != -1) {
            return false;
        }

        try {
            String asciiDnsName = IDN.toASCII(dnsName, IDN.USE_STD3_ASCII_RULES);
            int firstDot = asciiDnsName.indexOf('.');
            return firstDot > 0
                && !asciiDnsName.matches("[0-9.]+")
                && certificateDnsName.substring(1).equalsIgnoreCase(asciiDnsName.substring(firstDot));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static final class SniTrustManager implements X509TrustManager {
        private final X509TrustManager delegate;
        private final String serverName;

        private SniTrustManager(X509TrustManager delegate, String serverName) {
            this.delegate = delegate;
            this.serverName = serverName;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkServerTrusted(chain, authType);
            checkServerName(chain);
        }

        private void checkServerName(X509Certificate[] chain) throws CertificateException {
            // JDK-internal retries can open new connections without repeating the HTTP-level name check.
            if (!certificateMatchesServerName(chain, serverName)) {
                throw new CertificateException("Token proxy certificate does not match AZURE_KUBERNETES_SNI_NAME.");
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }
    }

    private static final class SniExtendedTrustManager extends X509ExtendedTrustManager {
        private final X509ExtendedTrustManager delegate;
        private final SniTrustManager sniTrustManager;

        private SniExtendedTrustManager(X509ExtendedTrustManager delegate, String serverName) {
            this.delegate = delegate;
            this.sniTrustManager = new SniTrustManager(delegate, serverName);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            sniTrustManager.checkServerTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
            delegate.checkServerTrusted(chain, authType, socket);
            sniTrustManager.checkServerName(chain);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
            delegate.checkServerTrusted(chain, authType, engine);
            sniTrustManager.checkServerName(chain);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
            delegate.checkClientTrusted(chain, authType, socket);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
            delegate.checkClientTrusted(chain, authType, engine);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }
    }

}
