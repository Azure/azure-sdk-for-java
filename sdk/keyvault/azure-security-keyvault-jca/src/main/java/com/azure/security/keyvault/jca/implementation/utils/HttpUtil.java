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
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * The REST client that uses the JDK {@link HttpURLConnection} class.
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

    static final int HTTP_TIMEOUT_IN_MILLISECONDS = 180_000;
    private static final int AIA_HTTP_TIMEOUT_IN_MILLISECONDS = 10_000;
    static final int MAX_AIA_RESPONSE_SIZE_IN_BYTES = 10 * 1024 * 1024;
    private static final int AIA_HTTP_TOTAL_TIMEOUT_IN_MILLISECONDS = 30_000;
    private static final int MAX_AIA_REDIRECTS = 5;

    /**
     * Functional interface for opening HTTP connections.
     *
     * <p>Introduced to be used for testing purposes, allowing the HTTP connection behavior to be mocked or overridden.
     */
    @FunctionalInterface
    interface ConnectionFactory {
        HttpURLConnection open(String url) throws IOException;
    }

    /**
     * Performs an HTTP GET request to the specified URI with the given headers.
     *
     * @param uri the URI to send the GET request to
     * @param headers the headers to include in the request
     * @return the response body as a string, or {@code null} if the request fails
     * @throws RuntimeException if the server returns a non-successful response
     */
    public static String get(String uri, Map<String, String> headers) {
        return get(uri, headers, HttpUtil::openConnection);
    }

    // Overloaded method that allows specifying a custom ConnectionFactory for testing purposes.
    static String get(String uri, Map<String, String> headers, ConnectionFactory connectionFactory) {
        HttpURLConnection connection = null;

        try {
            connection = connectionFactory.open(uri);
            configureConnection(connection, "GET", headers);

            ensureSuccessfulResponse(connection.getResponseCode());
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

    /**
     * Performs an HTTP POST request to the specified URI with the given headers, body, and content type.
     *
     * @param uri the URI to send the POST request to
     * @param headers the headers to include in the request
     * @param body the body of the POST request
     * @param contentType the content type of the POST request body
     * @return the response body as a string, or {@code null} if the request fails
     * @throws RuntimeException if the server returns a non-successful response
     */
    public static String post(String uri, Map<String, String> headers, String body, String contentType) {
        return post(uri, headers, body, contentType, HttpUtil::openConnection);
    }

    // Overloaded method that allows specifying a custom ConnectionFactory for testing purposes.
    static String post(String uri, Map<String, String> headers, String body, String contentType,
        ConnectionFactory connectionFactory) {

        HttpURLConnection connection = null;

        try {
            connection = connectionFactory.open(uri);
            configureConnection(connection, "POST", headers);
            connection.setDoOutput(true);

            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType);
            }

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
            }

            ensureSuccessfulResponse(connection.getResponseCode());
            return readResponseBody(connection);
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to finish the HTTP POST request.", ioe);

            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Performs an HTTP GET request and returns the response body along with HTTP metadata. Used primarily for
     * downloading DER-encoded certificates from CA Issuers URLs in AIA (Authority Information Access) certificate
     * extensions.
     *
     * @param url the URL to fetch
     * @return the response body bytes, or {@code null} if the request fails or returns non-2xx
     */
    public static BinaryHttpResponse getAiaBytesWithMetadata(String url) {
        return getAiaBytesWithMetadata(url, HttpUtil::openConnection);
    }

    // Overloaded method that allows specifying a custom ConnectionFactory for testing purposes.
    static BinaryHttpResponse getAiaBytesWithMetadata(String url, ConnectionFactory connectionFactory) {
        String currentUrl;

        try {
            currentUrl = validateAiaUrl(url);
        } catch (IllegalArgumentException e) {
            LOGGER.log(WARNING, "Unable to finish the HTTP GET (bytes) request for URL: " + url, e);

            return BinaryHttpResponse.empty();
        }

        // HttpURLConnection does not follow redirects between different protocols (e.g., HTTP to HTTPS)
        // reliably, so we handle redirects ourselves. AIA uses a small number of redirects, if any.
        for (int redirectCount = 0; redirectCount <= MAX_AIA_REDIRECTS; redirectCount++) {
            HttpURLConnection connection = null;

            try {
                connection = connectionFactory.open(currentUrl);

                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(AIA_HTTP_TIMEOUT_IN_MILLISECONDS);
                connection.setReadTimeout(AIA_HTTP_TIMEOUT_IN_MILLISECONDS);
                connection.setRequestProperty(USER_AGENT_KEY, USER_AGENT_VALUE);

                int status = connection.getResponseCode();
                String cacheControl = getCombinedHeaderValue(connection, "Cache-Control");
                String date = connection.getHeaderField("Date");
                String age = connection.getHeaderField("Age");
                String expires = connection.getHeaderField("Expires");

                if (isRedirect(status)) {
                    String location = connection.getHeaderField("Location");

                    if (location == null || redirectCount == MAX_AIA_REDIRECTS) {
                        LOGGER.log(WARNING, "HTTP GET redirect could not be followed for URL: {0}", currentUrl);

                        return new BinaryHttpResponse(null, cacheControl, date, age, expires);
                    }

                    currentUrl = resolveAiaRedirect(currentUrl, location);

                    continue;
                }

                if (status < 200 || status >= 300) {
                    LOGGER.log(WARNING, "HTTP GET returned status {0} for URL: {1}",
                        new Object[] { status, currentUrl });

                    return new BinaryHttpResponse(null, cacheControl, date, age, expires);
                }

                long contentLength = connection.getContentLengthLong();

                if (contentLength > MAX_AIA_RESPONSE_SIZE_IN_BYTES) {
                    LOGGER.log(WARNING, "AIA response exceeded the maximum size for URL: {0}", currentUrl);

                    return new BinaryHttpResponse(null, cacheControl, date, age, expires);
                }

                return new BinaryHttpResponse(readResponseBytes(connection.getInputStream(), currentUrl), cacheControl,
                    date, age, expires);
            } catch (IOException | IllegalArgumentException | ClassCastException | UncheckedIOException e) {
                // Catch all exceptions including IOException, IllegalArgumentException, and other runtime exceptions
                // that may occur during HTTP execution. Gracefully return null to allow AIA completion to fail silently
                // the entire jarsigner/signing operation.
                LOGGER.log(WARNING, "Unable to finish the HTTP GET (bytes) request for URL: " + currentUrl, e);

                return BinaryHttpResponse.empty();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        return BinaryHttpResponse.empty();
    }

    private static boolean isRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_PERM
            || status == HttpURLConnection.HTTP_MOVED_TEMP
            || status == HttpURLConnection.HTTP_SEE_OTHER
            || status == 307
            || status == 308;
    }

    private static String resolveAiaRedirect(String currentUrl, String location) {
        if (location.startsWith("?")) {
            int queryIndex = currentUrl.indexOf('?');
            int fragmentIndex = currentUrl.indexOf('#');
            int suffixIndex
                = queryIndex < 0 ? fragmentIndex : fragmentIndex < 0 ? queryIndex : Math.min(queryIndex, fragmentIndex);
            String currentUrlWithoutSuffix = suffixIndex < 0 ? currentUrl : currentUrl.substring(0, suffixIndex);

            return validateAiaUrl(currentUrlWithoutSuffix + location);
        }

        return validateAiaUrl(URI.create(currentUrl).resolve(location).toString());
    }

    private static String validateAiaUrl(String url) {
        URI uri = URI.create(url);
        String scheme = uri.getScheme();

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("AIA URL must use HTTP or HTTPS.");
        }

        return uri.toString();
    }

    private static byte[] readResponseBytes(InputStream inputStream, String url) throws IOException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(AIA_HTTP_TOTAL_TIMEOUT_IN_MILLISECONDS);

        try (InputStream responseBody = inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int totalBytesRead = 0;
            int read;

            while ((read = responseBody.read(buffer)) != -1) {
                if (read > MAX_AIA_RESPONSE_SIZE_IN_BYTES - totalBytesRead) {
                    LOGGER.log(WARNING, "AIA response exceeded the maximum size for URL: {0}", url);

                    return null;
                }

                outputStream.write(buffer, 0, read);

                totalBytesRead += read;

                if (System.nanoTime() > deadline) {
                    LOGGER.log(WARNING, "AIA response exceeded the maximum download time for URL: {0}", url);

                    return null;
                }
            }

            return outputStream.toByteArray();
        }
    }

    private static String getCombinedHeaderValue(HttpURLConnection connection, String name) {
        Map<String, List<String>> headers = connection.getHeaderFields();

        if (headers == null) {
            return null;
        }

        StringBuilder value = new StringBuilder();

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() == null || !name.equalsIgnoreCase(entry.getKey()) || entry.getValue() == null) {
                continue;
            }

            for (String headerValue : entry.getValue()) {
                if (headerValue == null) {
                    continue;
                }

                if (value.length() > 0) {
                    value.append(", ");
                }

                value.append(headerValue);
            }
        }

        return value.length() == 0 ? null : value.toString();
    }

    static final class BinaryHttpResponse {
        private final byte[] body;
        private final String cacheControl;
        private final String date;
        private final String age;
        private final String expires;

        BinaryHttpResponse(byte[] body, String cacheControl, String date, String age, String expires) {
            this.body = body;
            this.cacheControl = cacheControl;
            this.date = date;
            this.age = age;
            this.expires = expires;
        }

        private static BinaryHttpResponse empty() {
            return new BinaryHttpResponse(null, null, null, null, null);
        }

        byte[] getBody() {
            return body;
        }

        String getCacheControl() {
            return cacheControl;
        }

        String getDate() {
            return date;
        }

        String getAge() {
            return age;
        }

        String getExpires() {
            return expires;
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

    private static String createErrorMessage(int status) {
        return "Failed to get response from Key Vault because return http status code is " + status + ". It can be "
            + "caused by missing permissions or roles. To know how to add permissions or roles, see "
            + "https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-jca#prerequisites.";
    }

    private static void ensureSuccessfulResponse(int status) {
        if (status < 200 || status >= 300) {
            String errorMessage = createErrorMessage(status);
            LOGGER.log(SEVERE, errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    private static String readResponseBody(HttpURLConnection connection) throws IOException {
        try (InputStream responseBody = connection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            if (responseBody == null) {

                return null;
            }

            byte[] buffer = new byte[4096];
            int read;

            while ((read = responseBody.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Retrieves only the response headers from an HTTP GET request to the specified URI.
     *
     * @param uri the URI to send the HTTP GET request to
     * @return a map of response headers, or null if the request was not successful
     */
    public static Map<String, List<String>> getWithOnlyResponseHeaders(String uri) {
        return getWithOnlyResponseHeaders(uri, HttpUtil::openConnection);
    }

    // Overloaded method that allows specifying a custom ConnectionFactory for testing purposes.
    static Map<String, List<String>> getWithOnlyResponseHeaders(String uri, ConnectionFactory connectionFactory) {
        HttpURLConnection connection = null;

        try {
            connection = connectionFactory.open(uri);
            configureConnection(connection, "GET", null);

            if (connection.getResponseCode() == 401) {
                Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                Map<String, List<String>> responseHeaders = connection.getHeaderFields();

                if (responseHeaders != null) {
                    responseHeaders.forEach((name, values) -> {
                        if (name != null) {
                            headers.put(name, values);
                        }
                    });
                }

                return headers;
            }

            return null;
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to finish the HTTP GET request.", ioe);

            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void configureConnection(HttpURLConnection connection, String method, Map<String, String> headers)
        throws IOException {
        connection.setRequestMethod(method);
        connection.setConnectTimeout(HTTP_TIMEOUT_IN_MILLISECONDS);
        connection.setReadTimeout(HTTP_TIMEOUT_IN_MILLISECONDS);
        if (headers != null) {
            headers.forEach(connection::setRequestProperty);
        }
        connection.setRequestProperty(USER_AGENT_KEY, USER_AGENT_VALUE);
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
