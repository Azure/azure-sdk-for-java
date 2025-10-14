// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.security.keyvault.jca.implementation.JreKeyStoreFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * The RestClient that uses the Apache HttpClient class.
 */
public final class HttpUtil {
    public static final String DEFAULT_VERSION = "unknown";
    public static final String VERSION = Optional.of(HttpUtil.class)
        .map(Class::getPackage)
        .map(Package::getImplementationVersion)
        .orElse(DEFAULT_VERSION);

    public static final String HTTPS_PREFIX = "https://";
    public static final String API_VERSION_POSTFIX = "?api-version=7.1";
    public static final String USER_AGENT_VALUE = getUserAgentPrefix() + VERSION;

    static final String USER_AGENT_KEY = "User-Agent";
    static final String DEFAULT_USER_AGENT_VALUE_PREFIX = "az-se-kv-jca/";

    private static final Logger LOGGER = Logger.getLogger(HttpUtil.class.getName());

    public static String get(String uri, Map<String, String> headers) {
        String result = null;

        try (CloseableHttpClient client = buildClient()) {
            HttpGet httpGet = new HttpGet(uri);

            if (headers != null) {
                headers.forEach(httpGet::addHeader);
            }

            httpGet.addHeader(USER_AGENT_KEY, USER_AGENT_VALUE);

            result = client.execute(httpGet, createResponseHandler());
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to finish the HTTP GET request.", ioe);
        }

        return result;
    }

    public static String post(String uri, String body, String contentType) {
        return post(uri, null, body, contentType);
    }

    public static String getUserAgentPrefix() {
        return Optional.of(HttpUtil.class)
            .map(Class::getClassLoader)
            .map(c -> c.getResourceAsStream("azure-security-keyvault-jca-user-agent-value-prefix.txt"))
            .map(InputStreamReader::new)
            .map(BufferedReader::new)
            .map(BufferedReader::lines)
            .orElseGet(Stream::empty)
            .findFirst()
            .orElse(DEFAULT_USER_AGENT_VALUE_PREFIX);
    }

    public static String post(String uri, Map<String, String> headers, String body, String contentType) {
        String result = null;

        try (CloseableHttpClient client = buildClient()) {
            HttpPost httpPost = new HttpPost(uri);

            httpPost.addHeader(USER_AGENT_KEY, USER_AGENT_VALUE);

            if (headers != null) {
                headers.forEach(httpPost::addHeader);
                httpPost.addHeader("Content-Type", contentType);
            }

            httpPost.setEntity(new StringEntity(body, ContentType.create(contentType)));

            result = client.execute(httpPost, createResponseHandler());
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to finish the HTTP POST request.", ioe);
        }

        return result;
    }

    public static ClassicHttpResponse getWithResponse(String uri, Map<String, String> headers) {
        ClassicHttpResponse result = null;

        try (CloseableHttpClient client = buildClient()) {
            HttpGet httpGet = new HttpGet(uri);

            if (headers != null) {
                headers.forEach(httpGet::addHeader);
            }

            httpGet.addHeader(USER_AGENT_KEY, USER_AGENT_VALUE);

            result = client.execute(httpGet, createResponseHandlerForAuthChallenge());
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to finish the HTTP GET request.", ioe);
        }

        return result;
    }

    private static HttpClientResponseHandler<String> createResponseHandler() {
        return (ClassicHttpResponse response) -> {
            int status = response.getCode();
            String result;

            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                result = entity != null ? EntityUtils.toString(entity) : null;
            } else {
                String errorMessage = "Fail to get response from Key Vault because return http status code is " + status
                    + ". It "
                    + "can be caused by missing permissions or roles. To know how to add permissions or roles, see "
                    + "https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-jca#prerequisites.";
                LOGGER.log(SEVERE, errorMessage);
                throw new RuntimeException(errorMessage);
            }

            return result;
        };
    }

    private static HttpClientResponseHandler<ClassicHttpResponse> createResponseHandlerForAuthChallenge() {
        return (ClassicHttpResponse response) -> {
            int status = response.getCode();

            return status == 401 ? response : null;
        };
    }

    private static CloseableHttpClient buildClient() {
        KeyStore keyStore = JreKeyStoreFactory.getDefaultKeyStore();

        SSLContext sslContext = null;

        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, null).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.log(WARNING, "Unable to build the SSL context.", e);
        }

        SSLConnectionSocketFactory sslConnectionSocketFactory
            = new SSLConnectionSocketFactory(sslContext, (HostnameVerifier) null);

        PoolingHttpClientConnectionManager manager
            = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build());

        return HttpClients.custom().setConnectionManager(manager).build();
    }

    public static String validateUri(String uri, String propertyName) {
        if (uri == null) {
            StringBuilder messageBuilder = new StringBuilder();

            if (propertyName != null) {
                messageBuilder.append(propertyName);
            } else {
                messageBuilder.append("Provided URI ");
            }

            messageBuilder.append("cannot be null.");

            throw new NullPointerException(messageBuilder.toString());
        }

        if (!uri.startsWith(HTTPS_PREFIX)) {
            throw new IllegalArgumentException("Provided URI '" + uri + "' must start with 'https://'.");
        }

        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Provided URI '" + uri + "' is not a valid URI.");
        }

        return uri;
    }

    public static String addTrailingSlashIfRequired(String uri) {
        if (uri == null) {
            return null;
        }

        if (!uri.endsWith("/")) {
            return uri + "/";
        }

        return uri;
    }
}
