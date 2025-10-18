package com.azure.identity.implementation.customtokenproxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.MalformedParametersException;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.identity.implementation.util.IdentitySslUtil;

import reactor.core.publisher.Mono;

public class CustomTokenProxyHttpClient implements HttpClient {

    private final ProxyConfig proxyConfig;
    private volatile SSLContext cachedSSLContext;
    private volatile byte[] cachedFileContent;

    public CustomTokenProxyHttpClient(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.fromCallable(() -> sendSync(request, Context.NONE));
    }
    
    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
       try {
        HttpURLConnection connection = createConnection(request);
        connection.connect();
        return new CustomTokenProxyHttpResponse(request, connection);
       } catch (IOException e) {
        throw new RuntimeException("Failed to create connection to token proxy", e);
       }
    }

    // private HttpURLConnection createConnection(HttpRequest request) throws IOException {
    //     URL updateProxyRequest = rewriteTokenRequestForProxy(request.getUrl());
    //     HttpsURLConnection connection = (HttpsURLConnection) updateProxyRequest.openConnection();
    //     try {
    //         SSLContext sslContext = getSSLContext();
    //         connection.setSSLSocketFactory(sslContext.getSocketFactory());

    //         if(!CoreUtils.isNullOrEmpty(proxyConfig.getSniName())) {
    //             SSLParameters sslParameters = connection.getSSLParameters();
    //         }
    //     }
    //     return connection;

    //     // connection.setRequestMethod(request.getMethod().toString());
    //     // connection.setDoOutput(true);
    //     // request.getHeaders().forEach((key, values) -> {
    //     //     values.forEach(value -> connection.addRequestProperty(key, value));
    //     // });
    //     // return connection;
    // }

    private HttpURLConnection createConnection(HttpRequest request) throws IOException {
    URL updatedUrl = rewriteTokenRequestForProxy(request.getUrl());
    HttpsURLConnection connection = (HttpsURLConnection) updatedUrl.openConnection();

    // If SNI explicitly provided
    try {
        SSLContext sslContext = getSSLContext();
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        if(!CoreUtils.isNullOrEmpty(proxyConfig.getSniName())) {
            sslSocketFactory = new IdentitySslUtil.SniSslSocketFactory(sslSocketFactory, proxyConfig.getSniName());
        }
        connection.setSSLSocketFactory(sslSocketFactory);
    } catch (Exception e) {
        throw new RuntimeException("Failed to set up SSL context for token proxy", e);
    }

    connection.setRequestMethod(request.getHttpMethod().toString());
    // connection.setConnectTimeout(10_000);
    // connection.setReadTimeout(20_000);
    connection.setDoOutput(true);

    request.getHeaders().forEach(header -> {
        connection.addRequestProperty(header.getName(), header.getValue());
    });

    if (request.getBodyAsBinaryData() != null) {
        byte[] bytes = request.getBodyAsBinaryData().toBytes();
        if (bytes != null && bytes.length > 0) {
            connection.getOutputStream().write(bytes);
        }
    }

    return connection;
    }


    private URL rewriteTokenRequestForProxy(URL originalUrl) throws MalformedParametersException{
        try {
            String originalPath = originalUrl.getPath();
            String originalQuery = originalUrl.getQuery();

            String tokenProxyBase = proxyConfig.getTokenProxyUrl().toString();
            if(!tokenProxyBase.endsWith("/")) tokenProxyBase += "/";

            URI combined = URI.create(tokenProxyBase).resolve(originalPath.startsWith("/") ? originalPath.substring(1) : originalPath);

            String combinedStr = combined.toString();
            if (originalQuery != null && !originalQuery.isEmpty()) {
                combinedStr += "?" + originalQuery;
            }

            return new URL(combinedStr);            

        } catch (Exception e) {
            throw new RuntimeException("Failed to rewrite token request for proxy", e);
        }
    }

    private SSLContext getSSLContext() {
        try {
            // If no CA override provide, use default
           if (CoreUtils.isNullOrEmpty(proxyConfig.getCaFile())
            && (proxyConfig.getCaData() == null || proxyConfig.getCaData().length == 0)) {
                synchronized (this) {
                    if (cachedSSLContext == null) {
                        cachedSSLContext = SSLContext.getDefault();
                    }
                }
            return cachedSSLContext;
            }

            // If CA data provided, use it
            if (CoreUtils.isNullOrEmpty(proxyConfig.getCaFile())) {
                synchronized (this) {
                    if (cachedSSLContext == null) {
                        cachedSSLContext = createSslContextFromBytes(proxyConfig.getCaData());
                    }
                }
            return cachedSSLContext;
            }

            // If CA file provided, read it (and re-read if it changes)
            Path path = Paths.get(proxyConfig.getCaFile());
            if (!Files.exists(path)) {
                throw new IOException("CA file not found: " + proxyConfig.getCaFile());
            }

            byte[] currentContent;
            
            synchronized (this) {
                currentContent = Files.readAllBytes(path);
                if (currentContent.length == 0) {
                    throw new IOException("CA file " + proxyConfig.getCaFile() + " is empty");
                }

                if (cachedSSLContext == null || !Arrays.equals(currentContent, cachedFileContent)) {
                    cachedSSLContext = createSslContextFromBytes(currentContent);
                    cachedFileContent = currentContent;
                }
            }

            return cachedSSLContext;

        } catch (Exception e) {
                throw new RuntimeException("Failed to create default SSLContext", e);
        }
    }

    // Create SSLContext from byte array containing PEM certificate data
    private SSLContext createSslContextFromBytes(byte[] certificateData) {
        try (InputStream inputStream = new ByteArrayInputStream(certificateData)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(inputStream);
            return createSslContext(caCert);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext from bytes", e);
        }
    }

    // Create SSLContext from a single X509Certificate
    private SSLContext createSslContext(X509Certificate caCert) {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null);
            keystore.setCertificateEntry("ca-cert", caCert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext", e);
        }
    }

}
