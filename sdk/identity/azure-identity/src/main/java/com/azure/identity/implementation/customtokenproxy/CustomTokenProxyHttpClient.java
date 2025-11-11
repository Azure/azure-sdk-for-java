// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.SniSslSocketFactory;

import reactor.core.publisher.Mono;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CustomTokenProxyHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(CustomTokenProxyHttpClient.class);

    private final ProxyConfig proxyConfig;
    private volatile SSLContext cachedSSLContext;
    private volatile byte[] cachedFileContentHash;
    private volatile int cachedFileContentLength;
    private volatile SSLSocketFactory cachedSslSocketFactory;
    private final URL proxyUrl;
    private final String sniName;
    private final byte[] caData;
    private final String caFile;

    public CustomTokenProxyHttpClient(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
        this.proxyUrl = proxyConfig.getTokenProxyUrl();
        this.sniName = proxyConfig.getSniName();
        this.caData = proxyConfig.getCaData();
        this.caFile = proxyConfig.getCaFile();
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.fromCallable(() -> sendSync(request, Context.NONE));
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        try {
            HttpURLConnection connection = createConnection(request);
            return new CustomTokenProxyHttpResponse(request, connection);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to create connection to token proxy", e));
        }
    }

    private HttpURLConnection createConnection(HttpRequest request) throws IOException {
        URL updatedUrl = rewriteTokenRequestForProxy(request.getUrl());
        HttpsURLConnection connection = (HttpsURLConnection) updatedUrl.openConnection();
        try {
            SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
            if (!CoreUtils.isNullOrEmpty(sniName)) {
                sslSocketFactory = new SniSslSocketFactory(sslSocketFactory, sniName);
            }
            connection.setSSLSocketFactory(sslSocketFactory);
            connection.setHostnameVerifier(sniAwareVerifier(sniName, proxyUrl));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to set up SSL context for token proxy", e));
        }

        String method = request.getHttpMethod().toString();
        connection.setRequestMethod(method);
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(20_000);
        connection.setDoOutput(true);

        BinaryData bodyData = request.getBodyAsBinaryData();

        request.getHeaders().forEach(header -> {
            connection.addRequestProperty(header.getName(), header.getValue());
        });

        if (bodyData != null) {
            byte[] bytes = bodyData.toBytes();
            if (bytes != null && bytes.length > 0) {
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(bytes);
                    os.flush();
                }
            }
        }
        return connection;
    }

    private URL rewriteTokenRequestForProxy(URL originalUrl) throws MalformedURLException {
        try {
            String originalPath = originalUrl.getPath();
            String originalQuery = originalUrl.getQuery();

            String tokenProxyBase = proxyUrl.toString();
            if (!tokenProxyBase.endsWith("/")) {
                tokenProxyBase += "/";
            }

            URI combined = URI.create(tokenProxyBase)
                .resolve(originalPath.startsWith("/") ? originalPath.substring(1) : originalPath);

            String combinedStr = combined.toString();
            if (originalQuery != null && !originalQuery.isEmpty()) {
                combinedStr += "?" + originalQuery;
            }

            return URI.create(combinedStr).toURL();

        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to rewrite token request for proxy", e));
        }
    }

    private SSLSocketFactory getSSLSocketFactory() {
        SSLContext sslContext = getSSLContext();
        if (cachedSslSocketFactory == null) {
            synchronized (this) {
                if (cachedSslSocketFactory == null) {
                    cachedSslSocketFactory = sslContext.getSocketFactory();
                }
            }
        }
        return cachedSslSocketFactory;
    }

    private SSLContext getSSLContext() {
        try {
            // If no CA override provided, use default
            if (CoreUtils.isNullOrEmpty(caFile) && (caData == null || caData.length == 0)) {
                if (cachedSSLContext == null) {
                    synchronized (this) {
                        if (cachedSSLContext == null) {
                            cachedSSLContext = SSLContext.getDefault();
                            cachedSslSocketFactory = null;
                        }
                    }
                }
                return cachedSSLContext;
            }

            // If CA data provided, use it
            if (CoreUtils.isNullOrEmpty(caFile)) {
                if (cachedSSLContext == null) {
                    synchronized (this) {
                        if (cachedSSLContext == null) {
                            cachedSSLContext = createSslContextFromBytes(caData);
                            cachedSslSocketFactory = null;
                        }
                    }
                }
                return cachedSSLContext;
            }

            // If CA file provided, read it (and re-read if it changes)
            Path path = Paths.get(caFile);
            if (!Files.exists(path)) {
                throw LOGGER.logExceptionAsError(new RuntimeException("CA file not found: " + caFile));
            }

            byte[] currentContent = Files.readAllBytes(path);
            int currentLength = currentContent.length;
            byte[] currentHash = generateSHA256Hash(currentContent);

            synchronized (this) {
                if (currentLength == 0) {
                    if (cachedSSLContext == null) {
                        throw LOGGER.logExceptionAsError(new IllegalStateException("CA file " + caFile + " is empty"));
                    }
                    LOGGER.warning("CA file " + caFile + " is empty, using cached SSL context from previous load");
                    return cachedSSLContext;
                }

                if (cachedSSLContext == null
                    || currentLength != cachedFileContentLength
                    || !MessageDigest.isEqual(currentHash, cachedFileContentHash)) {
                    cachedSSLContext = createSslContextFromBytes(currentContent);
                    cachedFileContentLength = currentLength;
                    cachedFileContentHash = currentHash;
                    cachedSslSocketFactory = null;
                }
            }
            return cachedSSLContext;

        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to initialize SSLContext for proxy", e));
        }
    }

    // Create SSLContext from byte array containing PEM certificate data
    private SSLContext createSslContextFromBytes(byte[] certificateData) {
        try (InputStream inputStream = new ByteArrayInputStream(certificateData)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            List<X509Certificate> certificates = new ArrayList<>();
            while (true) {
                try {
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
                    certificates.add(cert);
                } catch (CertificateException e) {
                    break;
                }
            }

            if (certificates.isEmpty()) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("No valid certificates found"));
            }
            return createSslContext(certificates);
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to create SSLContext from bytes", e));
        }
    }

    private SSLContext createSslContext(List<X509Certificate> certificates) {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null);
            int index = 1;
            for (X509Certificate caCert : certificates) {
                keystore.setCertificateEntry("ca-cert-" + index, caCert);
                index++;
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context;
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to create SSLContext", e));
        }
    }

    private static HostnameVerifier sniAwareVerifier(String sniName, URL customProxyUrl) {
        return (urlHost, session) -> {
            String peerHost = session.getPeerHost();
            String proxyHost = customProxyUrl.getHost();

            if (peerHost.equalsIgnoreCase(proxyHost)) {
                if (!CoreUtils.isNullOrEmpty(sniName)) {
                    try {
                        Certificate[] certificates = session.getPeerCertificates();
                        if (certificates.length > 0 && certificates[0] instanceof X509Certificate) {
                            X509Certificate cert = (X509Certificate) certificates[0];
                            return certificateContainsDnsName(cert, sniName);
                        }
                        return false;
                    } catch (SSLPeerUnverifiedException e) {
                        return false;
                    }
                }
                return true;
            }

            return false;
        };
    }

    private static boolean certificateContainsDnsName(X509Certificate cert, String dnsName) {
        try {
            Collection<List<?>> sanList = cert.getSubjectAlternativeNames();
            if (sanList != null) {
                for (List<?> san : sanList) {
                    if (san.size() >= 2 && san.get(0).equals(2)) {
                        String certDnsName = (String) san.get(1);
                        if (certDnsName.equalsIgnoreCase(dnsName)) {
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

    private static byte[] generateSHA256Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(data);
            return encodedHash;
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("SHA-256 algorithm not found", e));
        }
    }
}
