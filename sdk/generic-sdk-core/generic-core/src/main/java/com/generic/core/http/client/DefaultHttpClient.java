// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.Response;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.ProxyOptions;
import com.generic.core.http.models.ServerSentEvent;
import com.generic.core.http.models.ServerSentEventListener;
import com.generic.core.implementation.AccessibleByteArrayOutputStream;
import com.generic.core.implementation.http.ContentType;
import com.generic.core.implementation.util.ServerSentEventHelper;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Header;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import com.generic.core.util.ClientLogger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
class DefaultHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultHttpClient.class);
    private final long connectionTimeout;
    private final long readTimeout;
    private final ProxyOptions proxyOptions;
    private final HttpConnectionCache httpConnectionCache;
    private static final String LAST_EVENT_ID = "Last-Event-Id";
    private static final String DEFAULT_EVENT = "message";
    private static final Pattern DIGITS_ONLY = Pattern.compile("^[\\d]*$");

    DefaultHttpClient(Duration connectionTimeout, Duration readTimeout, int maximumConnections, boolean connectionKeepAlive, ProxyOptions proxyOptions) {
        this.connectionTimeout = connectionTimeout == null ? -1 : connectionTimeout.toMillis();
        this.readTimeout = readTimeout == null ? -1 : readTimeout.toMillis();
        this.proxyOptions = proxyOptions;
        this.httpConnectionCache = HttpConnectionCache.getInstance(connectionKeepAlive, maximumConnections);
    }

    /**
     * Synchronously send the HttpRequest.
     *
     * @param httpRequest The HTTP request being sent
     * @return The Response object
     */
    @Override
    public Response<?> send(HttpRequest httpRequest) {
        HttpConnectionCache.HttpConnection connection = httpConnectionCache.get(new HttpConnectionProperties(httpRequest, httpRequest.getUrl(), proxyOptions));

        if (connection.getUrlConnection() != null) {
            HttpURLConnection urlConnection = connection.getUrlConnection();
            setUrlConnectionProperties(httpRequest, urlConnection);
            sendBody(httpRequest, urlConnection);
            return receiveResponse(httpRequest, urlConnection);
        } else {
            return SocketClient.sendPatchRequest(httpRequest, connection.getSocketInputStream(), connection.getSocketOutputStream());
        }
    }

    private void setUrlConnectionProperties(HttpRequest httpRequest, HttpURLConnection urlConnection) {
        if (connectionTimeout != -1) {
            urlConnection.setConnectTimeout((int) connectionTimeout);
        }

        if (readTimeout != -1) {
            urlConnection.setReadTimeout((int) readTimeout);
        }

        try {
            urlConnection.setRequestMethod(httpRequest.getHttpMethod().toString());
        } catch (ProtocolException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }

        for (Header header : httpRequest.getHeaders()) {
            for (String value : header.getValues()) {
                urlConnection.addRequestProperty(header.getName(), value);
            }
        }
    }

    /**
     * Synchronously sends the content of an HttpRequest via an HttpUrlConnection instance.
     *
     * @param httpRequest The HTTP Request being sent
     * @param connection  The HttpURLConnection that is being sent to
     */
    private void sendBody(HttpRequest httpRequest, HttpURLConnection connection) {
        BinaryData body = httpRequest.getBody();
        if (body == null) {
            return;
        }

        HttpMethod method = httpRequest.getHttpMethod();
        switch (httpRequest.getHttpMethod()) {
            case GET:
            case HEAD:
                return;

            case OPTIONS:
            case TRACE:
            case CONNECT:
            case POST:
            case PUT:
            case DELETE:
                connection.setDoOutput(true);

                try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
                    body.writeTo(os);
                    os.flush();
                } catch (IOException e) {
                    throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
                }
                return;

            default:
                throw LOGGER.logThrowableAsError(new IllegalStateException("Unknown HTTP Method: " + method));
        }
    }

    /**
     * Receive the response from the remote server
     *
     * @param httpRequest The HTTP Request being sent
     * @param connection  The HttpURLConnection being sent to
     * @return A HttpResponse object
     */
    private Response<?> receiveResponse(HttpRequest httpRequest, HttpURLConnection connection) {
        try {
            int responseCode = connection.getResponseCode();
            Headers responseHeaders = getResponseHeaders(connection);

            ServerSentEventListener listener = httpRequest.getServerSentEventListener();
            if (connection.getErrorStream() == null && isTextEventStream(responseHeaders)) {
                if (listener != null) {
                    processTextEventStream(httpRequest, connection, listener);
                } else {
                    LOGGER.atInfo().log(() -> "No listener attached to the server sent "
                        + "event http request. Treating response as regular response.");
                }

                return new HttpResponse<>(httpRequest, responseCode, responseHeaders, null);
            } else {
                AccessibleByteArrayOutputStream outputStream = getAccessibleByteArrayOutputStream(connection);

                return new HttpResponse<>(httpRequest, responseCode, responseHeaders,
                    BinaryData.fromByteBuffer(outputStream.toByteBuffer()));
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } finally {
            connection.disconnect();
        }
    }

    private void processTextEventStream(HttpRequest httpRequest, HttpURLConnection connection,
                                        ServerSentEventListener listener) {
        RetrySSEResult retrySSEResult;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            retrySSEResult = processBuffer(reader, listener);
            if (retrySSEResult != null) {
                retryExceptionForSSE(retrySSEResult, listener, httpRequest);
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private static boolean isTextEventStream(Headers responseHeaders) {
        return Objects.equals(ContentType.TEXT_EVENT_STREAM, responseHeaders.getValue(HeaderName.CONTENT_TYPE));
    }

    /**
     * Processes the sse buffer and dispatches the event
     *
     * @param reader   The BufferedReader object
     * @param listener The listener object attached with the httpRequest
     * @return A retry result if a retry is needed, otherwise null
     */
    private RetrySSEResult processBuffer(BufferedReader reader, ServerSentEventListener listener) {
        StringBuilder collectedData = new StringBuilder();
        ServerSentEvent event = null;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                collectedData.append(line).append("\n");
                if (isEndOfBlock(collectedData)) {
                    event = processLines(collectedData.toString().split("\n"));
                    if (!Objects.equals(event.getEvent(), DEFAULT_EVENT) || event.getData() != null) {
                        listener.onEvent(event);
                    }
                    collectedData = new StringBuilder(); // clear the collected data
                }
            }
            listener.onClose();
        } catch (IOException e) {
            if (event != null) {
                return new RetrySSEResult(e, event.getId(), ServerSentEventHelper.getRetryAfter(event));
            } else {
                return new RetrySSEResult(e, -1, null);
            }
        }
        return null;
    }

    private boolean isEndOfBlock(StringBuilder sb) {
        // blocks of data are separated by double newlines
        // add more end of blocks here if needed
        return sb.indexOf("\n\n") >= 0;
    }

    private ServerSentEvent processLines(String[] lines) {
        List<String> eventData = null;
        ServerSentEvent event = new ServerSentEvent();

        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx == 0) {
                ServerSentEventHelper.setComment(event, line.substring(1).trim());
                continue;
            }
            String field = line.substring(0, idx < 0 ? lines.length : idx).trim().toLowerCase();
            String value = idx < 0 ? "" : line.substring(idx + 1).trim();

            switch (field) {
                case "event":
                    ServerSentEventHelper.setEvent(event, value);
                    break;
                case "data":
                    if (eventData == null) {
                        eventData = new ArrayList<>();
                    }
                    eventData.add(value);
                    break;
                case "id":
                    if (!value.isEmpty()) {
                        ServerSentEventHelper.setId(event, Long.parseLong(value));
                    }
                    break;
                case "retry":
                    if (!value.isEmpty() && DIGITS_ONLY.matcher(value).matches()) {
                        ServerSentEventHelper.setRetryAfter(event, Duration.ofMillis(Long.parseLong(value)));
                    }
                    break;
                default:
                    throw LOGGER.logThrowableAsWarning(
                        new IllegalArgumentException("Invalid data received from server"));
            }
        }

        if (event.getEvent() == null) {
            ServerSentEventHelper.setEvent(event, DEFAULT_EVENT);
        }
        if (eventData != null) {
            ServerSentEventHelper.setData(event, eventData);
        }

        return event;
    }

    /**
     * Retries the request if the listener allows it
     *
     * @param retrySSEResult the result of the retry
     * @param listener The listener object attached with the httpRequest
     * @param httpRequest the HTTP Request being sent
     */
    private void retryExceptionForSSE(RetrySSEResult retrySSEResult, ServerSentEventListener listener,
                                      HttpRequest httpRequest) {
        if (Thread.currentThread().isInterrupted() || !listener.shouldRetry(retrySSEResult.getException(),
            retrySSEResult.getRetryAfter(), retrySSEResult.getLastEventId())) {
            listener.onError(retrySSEResult.getException());
            return;
        }

        if (retrySSEResult.getLastEventId() != -1) {
            httpRequest.getHeaders()
                .add(HeaderName.fromString(LAST_EVENT_ID), String.valueOf(retrySSEResult.getLastEventId()));
        }

        try {
            if (retrySSEResult.getRetryAfter() != null) {
                Thread.sleep(retrySSEResult.getRetryAfter().toMillis());
            }
        } catch (InterruptedException ignored) {
            return;
        }

        if (!Thread.currentThread().isInterrupted()) {
            this.send(httpRequest);
        }
    }

    private Headers getResponseHeaders(HttpURLConnection connection) {
        Map<String, List<String>> hucHeaders = connection.getHeaderFields();
        Headers responseHeaders = new Headers(hucHeaders.size());
        for (Map.Entry<String, List<String>> entry : hucHeaders.entrySet()) {
            if (entry.getKey() != null) {
                responseHeaders.add(HeaderName.fromString(entry.getKey()), entry.getValue());
            }
        }
        return responseHeaders;
    }

    private static AccessibleByteArrayOutputStream getAccessibleByteArrayOutputStream(HttpURLConnection connection)
        throws IOException {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();

        try (InputStream errorStream = connection.getErrorStream();
             InputStream inputStream = (errorStream == null) ? connection.getInputStream() : errorStream) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        }
        return outputStream;
    }

    /**
     * Inner class to hold the result for a retry of an SSE request
     */
    private static class RetrySSEResult {
        private final long lastEventId;
        private final Duration retryAfter;
        private final IOException ioException;

        RetrySSEResult(IOException e, long lastEventId, Duration retryAfter) {
            this.ioException = e;
            this.lastEventId = lastEventId;
            this.retryAfter = retryAfter;
        }

        public long getLastEventId() {
            return lastEventId;
        }

        public Duration getRetryAfter() {
            return retryAfter;
        }

        public IOException getException() {
            return ioException;
        }
    }

    private static class HttpConnectionCache {
        private static HttpConnectionCache INSTANCE;
        private final int maxConnections;
        private final Map<HttpConnectionProperties, List<HttpConnection>> connectionPool
            = new HashMap<HttpConnectionProperties, List<HttpConnection>>();

        private HttpConnectionCache(boolean connectionKeepAlive, int maximumConnections) {
            if (!connectionKeepAlive) {
                maxConnections = 0;
            } else {
                this.maxConnections = maximumConnections;
            }
        }

        public static synchronized HttpConnectionCache getInstance(boolean connectionKeepAlive, int maximumConnections) {
            if (INSTANCE == null) {
                INSTANCE = new HttpConnectionCache(connectionKeepAlive, maximumConnections);
            }
            return INSTANCE;
        }

        public HttpConnection get(HttpConnectionProperties httpConnectionProperties) {

            // Try-Get a connection from the cache
            synchronized (connectionPool) {
                List<HttpConnection> connections = connectionPool.get(httpConnectionProperties);
                if (connections != null) {
                    for (HttpConnection connection : connections) {
                        if (connection.getConnectionProperties().equals(httpConnectionProperties)) {
                            return connection;
                        }
                    }
                }
            }

            // If no connection is available, create a new one
            HttpConnection connection;
            // If the request is a PATCH request, we need to use a socket connection
            if (httpConnectionProperties.getHttpRequest().getHttpMethod().equals(HttpMethod.PATCH)) {
                connection = getSocketHttpConnection(httpConnectionProperties);
            } else if (httpConnectionProperties.getProxyOptions() != null) {
                // If a proxy is specified, create a connection with the proxy
                connection = getProxyHttpConnection(httpConnectionProperties);
            } else {
                try {
                    connection = new HttpConnection((HttpURLConnection) httpConnectionProperties.getUrl().openConnection(), httpConnectionProperties);
                } catch (IOException e) {
                    throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
                }
            }

            synchronized (connectionPool) {
                List<HttpConnection> connections = connectionPool.get(httpConnectionProperties);
                if (connections == null) {
                    connections = new ArrayList<>();
                    connectionPool.put(httpConnectionProperties, connections);
                }
                if (connections.size() < maxConnections) {
                    connections.add(connection);
                }
            }
            return connection;
        }

        private static HttpConnection getSocketHttpConnection(HttpConnectionProperties httpConnectionProperties) {
            HttpConnection connection;
            URL requestUrl = httpConnectionProperties.getUrl();
            String protocol = requestUrl.getProtocol();
            String host = requestUrl.getHost();
            int port = requestUrl.getPort();
            try (Socket socket = protocol.equals("https")
                ? (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port)
                : new Socket(host, port);
                 OutputStream outputStream = socket.getOutputStream();
                 InputStream inputStream = socket.getInputStream()) {
                connection = new HttpConnection(inputStream, outputStream, httpConnectionProperties);
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }
            return connection;
        }

        private HttpConnection getProxyHttpConnection(HttpConnectionProperties httpConnectionProperties) {
            HttpURLConnection connection;
            URL url = httpConnectionProperties.getUrl();
            ProxyOptions proxyOptions = httpConnectionProperties.getProxyOptions();
            // If a proxy is specified, the authorization type will default to 'Basic' unless Digest authentication is
            // specified in the 'Authorization' header.
            try {
                InetSocketAddress address = proxyOptions.getAddress();

                if (address != null) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
                    connection = (HttpURLConnection) url.openConnection(proxy);

                    if (proxyOptions.getUsername() != null && proxyOptions.getPassword() != null) {
                        String authString = proxyOptions.getUsername() + ":" + proxyOptions.getPassword();
                        String authStringEnc = Base64.getEncoder().encodeToString(authString.getBytes());
                        connection.setRequestProperty("Proxy-Authorization", "Basic " + authStringEnc);
                    }
                } else {
                    throw LOGGER.logThrowableAsWarning(new ConnectException("Invalid proxy address"));
                }
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }
            return new HttpConnection(connection, httpConnectionProperties);
        }

        private static class HttpConnection {
            private final HttpURLConnection urlConnection;
            private final OutputStream socketOutputStream;
            private final InputStream socketInputStream;
            private final HttpConnectionProperties connectionProperties;

            HttpConnection(HttpURLConnection connection, HttpConnectionProperties connectionProperties) {
                this.urlConnection = connection;
                this.socketInputStream = null;
                this.socketOutputStream = null;
                this.connectionProperties = connectionProperties;
            }

            HttpConnection(InputStream inputStream, OutputStream outputStream, HttpConnectionProperties connectionProperties) {
                this.urlConnection = null;
                this.socketInputStream = inputStream;
                this.socketOutputStream = outputStream;
                this.connectionProperties = connectionProperties;
            }

            HttpURLConnection getUrlConnection() {
                return urlConnection;
            }

            OutputStream getSocketOutputStream() {
                return socketOutputStream;
            }

            InputStream getSocketInputStream() {
                return socketInputStream;
            }

            HttpConnectionProperties getConnectionProperties() {
                return connectionProperties;
            }
        }
    }

    private static class HttpConnectionProperties {
        private final HttpRequest httpRequest;
        private final URL url;
        private final ProxyOptions proxyOptions;

        HttpConnectionProperties(HttpRequest httpRequest, URL requestUrl, ProxyOptions proxyOptions) {
            this.httpRequest = httpRequest;
            this.url = requestUrl;
            this.proxyOptions = proxyOptions;
        }

        @Override public boolean equals(Object other) {
            if (other instanceof HttpConnectionProperties) {
                HttpConnectionProperties that = (HttpConnectionProperties) other;
                return Objects.equals(this.proxyOptions, that.proxyOptions)
                    && this.url.equals(that.url);
            }
            return false;
        }
        @Override public int hashCode() {
            int result = 17;
            result = 31 * result + (proxyOptions != null ? proxyOptions.hashCode() : 0);
            return result;
        }

        URL getUrl() {
            return url;
        }

        HttpRequest getHttpRequest() {
            return httpRequest;
        }

        ProxyOptions getProxyOptions() {
            return proxyOptions;
        }
    }

    private static class SocketClient {

        private static final String HTTP_VERSION = " HTTP/1.1";

        /**
         * Calls buildAndSend to send a String representation of the request across the output stream, then calls
         * buildResponse to get an instance of HttpUrlConnectionResponse from the input stream
         *
         * @param httpRequest The HTTP Request being sent
         * @param inputStream the input stream from the socket
         * @param outputStream the output stream from the socket for writing the request
         * @return an instance of Response
         */
        private static Response<?> sendPatchRequest(HttpRequest httpRequest, InputStream inputStream, OutputStream outputStream) {
            httpRequest.getHeaders().set(HeaderName.HOST, httpRequest.getUrl().getHost());
            if (!"keep-alive".equalsIgnoreCase(httpRequest.getHeaders().getValue(HeaderName.CONNECTION))) {
                httpRequest.getHeaders().set(HeaderName.CONNECTION, "close");
            }

            try (BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                 OutputStreamWriter out = new OutputStreamWriter(outputStream)) {

                buildAndSend(httpRequest, out);

                Response<?> response = buildResponse(httpRequest, in);
                Header locationHeader = response.getHeaders().get(HeaderName.LOCATION);
                String redirectLocation = (locationHeader == null) ? null : locationHeader.getValue();

                if (redirectLocation != null) {
                    if (redirectLocation.startsWith("http")) {
                        httpRequest.setUrl(redirectLocation);
                    } else {
                        httpRequest.setUrl(new URL(httpRequest.getUrl(), redirectLocation));
                    }
                    return sendPatchRequest(httpRequest, inputStream, outputStream);
                }
                return response;
            }  catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }
        }

        /**
         * Converts an instance of HttpRequest to a String representation for sending over the output stream
         *
         * @param httpRequest The HTTP Request being sent
         * @param out         output stream for writing the request
         * @throws IOException If an I/O error occurs
         */
        private static void buildAndSend(HttpRequest httpRequest, OutputStreamWriter out) throws IOException {
            final StringBuilder request = new StringBuilder();

            request.append("PATCH ").append(httpRequest.getUrl().getPath()).append(HTTP_VERSION).append("\r\n");

            if (httpRequest.getHeaders().getSize() > 0) {
                for (Header header : httpRequest.getHeaders()) {
                    header.getValues()
                        .forEach(value -> request.append(header.getName()).append(':').append(value).append("\r\n"));
                }
            }
            if (httpRequest.getBody() != null) {
                request.append("\r\n").append(httpRequest.getBody().toString()).append("\r\n");
            }

            out.write(request.toString());
            out.flush();
        }

        /**
         * Reads the response from the input stream and extracts the information needed to construct an instance of
         * HttpUrlConnectionResponse
         *
         * @param httpRequest The HTTP Request being sent
         * @param reader      the input stream from the socket
         * @return an instance of HttpUrlConnectionResponse
         * @throws IOException If an I/O error occurs
         */
        private static Response<?> buildResponse(HttpRequest httpRequest, BufferedReader reader) throws IOException {
            String statusLine = reader.readLine();
            int dotIndex = statusLine.indexOf('.');
            int statusCode = Integer.parseInt(statusLine.substring(dotIndex + 3, dotIndex + 6));

            Headers headers = new Headers();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Headers may have optional leading and trailing whitespace around the header value.
                // https://tools.ietf.org/html/rfc7230#section-3.2
                // Process this accordingly.
                int split = line.indexOf(':'); // Find ':' to split the header name and value.
                String key = line.substring(0, split); // Get the header name.
                String value = line.substring(split + 1).trim(); // Get the header value and trim whitespace.
                headers.add(HeaderName.fromString(key), value);
            }

            StringBuilder bodyString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                bodyString.append(line);
            }

            BinaryData body = BinaryData.fromByteBuffer(ByteBuffer.wrap(bodyString.toString().getBytes()));

            return new HttpResponse<>(httpRequest, statusCode, headers, body);
        }
    }
}
