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
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentitySslUtil;

import reactor.core.publisher.Mono;

public class CustomTokenProxyHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(CustomTokenProxyHttpClient.class);

    private final ProxyConfig proxyConfig;
    private volatile SSLContext cachedSSLContext;
    private volatile byte[] cachedFileContent;
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
        // If SNI explicitly provided
        try {
            SSLContext sslContext = getSSLContext();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            if (!CoreUtils.isNullOrEmpty(sniName)) {
                sslSocketFactory = new IdentitySslUtil.SniSslSocketFactory(sslSocketFactory, sniName);
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

        boolean hasBody = request.getBodyAsBinaryData() != null && request.getBodyAsBinaryData().toBytes() != null;

        if (hasBody && ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
            connection.setDoOutput(true);
        } else {
            connection.setDoOutput(false);
        }

        request.getHeaders().forEach(header -> {
            connection.addRequestProperty(header.getName(), header.getValue());
        });

        if (request.getBodyAsBinaryData() != null) {
            byte[] bytes = request.getBodyAsBinaryData().toBytes();
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

            return new URL(combinedStr);

        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to rewrite token request for proxy", e));
        }
    }

    private SSLContext getSSLContext() {
        try {
            // If no CA override provided, use default
            if (CoreUtils.isNullOrEmpty(caFile) && (caData == null || caData.length == 0)) {
                synchronized (this) {
                    if (cachedSSLContext == null) {
                        cachedSSLContext = SSLContext.getDefault();
                    }
                }
                return cachedSSLContext;
            }

            // If CA data provided, use it
            if (CoreUtils.isNullOrEmpty(caFile)) {
                synchronized (this) {
                    if (cachedSSLContext == null) {
                        cachedSSLContext = createSslContextFromBytes(caData);
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

            synchronized (this) {
                if (currentContent.length == 0) {
                    throw LOGGER.logExceptionAsError(new RuntimeException("CA file " + caFile + " is empty"));
                }

                if (cachedSSLContext == null || !Arrays.equals(currentContent, cachedFileContent)) {
                    cachedSSLContext = createSslContextFromBytes(currentContent);
                    cachedFileContent = currentContent;
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
                throw LOGGER.logExceptionAsError(new RuntimeException("No valid certificates found"));
            }
            return createSslContext(certificates);
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to create SSLContext from bytes", e));
        }
    }

    // Create SSLContext from a single X509Certificate
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
            String expectedProxyHost = customProxyUrl.getHost();
            return peerHost.equalsIgnoreCase(expectedProxyHost)
                || (!CoreUtils.isNullOrEmpty(sniName) && peerHost.equalsIgnoreCase(sniName));
        };
    }

}
