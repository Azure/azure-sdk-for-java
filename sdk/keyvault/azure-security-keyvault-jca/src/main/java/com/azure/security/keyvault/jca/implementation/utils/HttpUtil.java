// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.security.keyvault.jca.implementation.JreKeyStoreFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
        HttpURLConnection connection = null;
        try {
            connection = openConnection(uri);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            if (headers != null) {
                headers.forEach(connection::setRequestProperty);
            }
            connection.setRequestProperty(USER_AGENT_KEY, USER_AGENT_VALUE);

            return readResponseBody(connection);
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to finish the HTTP GET request.", ioe);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
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
        HttpURLConnection connection = null;
        try {
            connection = openConnection(uri);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            if (headers != null) {
                headers.forEach(connection::setRequestProperty);
            }
            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            connection.setRequestProperty(USER_AGENT_KEY, USER_AGENT_VALUE);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            if (status >= 200 && status < 300) {
                return readResponseBody(connection);
            } else {
                LOGGER.log(SEVERE, createErrorMessage(status));
                return "";
            }
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to finish the HTTP POST request.", ioe);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String createErrorMessage(int status) {
        return "Fail to get response from Key Vault because return http status code is " + status + ". It can be "
            + "caused by missing permissions or roles. To know how to add permissions or roles, see "
            + "https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-jca#prerequisites.";
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    private static String readResponseBody(HttpURLConnection connection) throws IOException {
        InputStream responseBody
            = (connection.getErrorStream() != null) ? connection.getErrorStream() : connection.getInputStream();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = responseBody.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    public static Map<String, List<String>> getWithResponseHeadersOnlyReturn(String uri) {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(uri);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            connection.setRequestProperty(USER_AGENT_KEY, USER_AGENT_VALUE);

            if (connection.getResponseCode() == 401) {
                return null;
            } else {
                return connection.getHeaderFields();
            }
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to finish the HTTP GET request.", ioe);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static HttpURLConnection openConnection(String uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(uri).toURL().openConnection();
            if (connection instanceof HttpsURLConnection) {
                try {
                    TrustManagerFactory trustManagerFactory
                        = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(JreKeyStoreFactory.getDefaultKeyStore());
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
                    ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
                    LOGGER.log(WARNING, "Unable to build the SSL context.", e);
                }
            }

            return connection;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
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
