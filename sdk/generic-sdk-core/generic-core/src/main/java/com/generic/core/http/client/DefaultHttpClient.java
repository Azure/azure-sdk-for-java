// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.ProxyOptions;
import com.generic.core.http.models.ServerSentEvent;
import com.generic.core.http.models.ServerSentEventListener;
import com.generic.core.implementation.AccessibleByteArrayOutputStream;
import com.generic.core.implementation.http.ContentType;
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
    private static final String LAST_EVENT_ID = "Last-Event-Id";
    private static final String DEFAULT_EVENT = "message";
    private final Pattern DIGITS_ONLY = Pattern.compile("^[\\d]*$");

    DefaultHttpClient(Duration connectionTimeout, Duration readTimeout, ProxyOptions proxyOptions) {
        this.connectionTimeout = connectionTimeout == null ? -1 : connectionTimeout.toMillis();
        this.readTimeout = readTimeout == null ? -1 : readTimeout.toMillis();
        this.proxyOptions = proxyOptions;
    }

    /**
     * Synchronously send the HttpRequest.
     *
     * @param httpRequest The HTTP request being sent
     * @return The HttpResponse object
     */
    @Override
    public HttpResponse send(HttpRequest httpRequest) {
        if (httpRequest.getHttpMethod() == HttpMethod.PATCH) {
            return sendPatchViaSocket(httpRequest);
        }

        HttpURLConnection connection = connect(httpRequest);

        sendBody(httpRequest, connection);

        return receiveResponse(httpRequest, connection);
    }

    /**
     * Synchronously sends a PATCH request via a socket client.
     *
     * @param httpRequest The HTTP request being sent
     * @return The HttpResponse object
     */
    private HttpResponse sendPatchViaSocket(HttpRequest httpRequest) {
        try {
            return SocketClient.sendPatchRequest(httpRequest);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsWarning(new UncheckedIOException(e));
        }
    }

    /**
     * Open a connection based on the HttpRequest URL
     *
     * <p>If a proxy is specified, the authorization type will default to 'Basic' unless Digest authentication is
     * specified in the 'Authorization' header.</p>
     *
     * @param httpRequest The HTTP Request being sent
     * @return The HttpURLConnection object
     */
    private HttpURLConnection connect(HttpRequest httpRequest) {
        try {
            HttpURLConnection connection;
            URL url = httpRequest.getUrl();

            if (proxyOptions != null) {
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
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            if (connectionTimeout != -1) {
                connection.setConnectTimeout((int) connectionTimeout);
            }

            if (readTimeout != -1) {
                connection.setReadTimeout((int) readTimeout);
            }

            try {
                connection.setRequestMethod(httpRequest.getHttpMethod().toString());
            } catch (ProtocolException e) {
                throw LOGGER.logThrowableAsError(new RuntimeException(e));
            }

            for (Header header : httpRequest.getHeaders()) {
                for (String value : header.getValues()) {
                    connection.addRequestProperty(header.getName(), value);
                }
            }

            return connection;
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Synchronously sends the content of an HttpRequest via an HttpUrlConnection instance.
     *
     * @param httpRequest The HTTP Request being sent
     * @param connection The HttpURLConnection that is being sent to
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
     * @param connection The HttpURLConnection being sent to
     * @return A HttpResponse object
     */
    private HttpResponse receiveResponse(HttpRequest httpRequest, HttpURLConnection connection) {
        try {
            int responseCode = connection.getResponseCode();
            Headers responseHeaders = getResponseHeaders(connection);

            ServerSentEventListener listener = httpRequest.getServerSentEventListener();
            if (connection.getErrorStream() == null && isTextEventStream(responseHeaders)) {
                if (listener != null) {
                    processTextEventStream(httpRequest, connection, listener);
                } else {
                    LOGGER.log(ClientLogger.LogLevel.INFORMATIONAL, () -> "No listener attached to the server sent event" +
                            " http request. Treating response as regular response.");
                }
                return new DefaultHttpClientResponse(httpRequest, responseCode, responseHeaders);
            } else {
                AccessibleByteArrayOutputStream outputStream = getAccessibleByteArrayOutputStream(connection);
                return new DefaultHttpClientResponse(httpRequest, responseCode, responseHeaders,
                    BinaryData.fromByteBuffer(outputStream.toByteBuffer()));
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } finally {
            connection.disconnect();
        }
    }

    private void processTextEventStream(HttpRequest httpRequest, HttpURLConnection connection, ServerSentEventListener listener) {
        RetrySSEResult retrySSEResult;
        try (BufferedReader reader
                 = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            retrySSEResult = processBuffer(reader, listener);
            if (retrySSEResult != null) {
                retryExceptionForSSE(retrySSEResult, listener, httpRequest);
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private boolean isTextEventStream(Headers responseHeaders) {
        return responseHeaders.get(HeaderName.CONTENT_TYPE) != null &&
        responseHeaders.get(HeaderName.CONTENT_TYPE).getValue().equals(ContentType.TEXT_EVENT_STREAM);
    }

    /**
     * Processes the sse buffer and dispatches the event
     *
     * @param reader   The BufferedReader object
     * @param listener The listener object attached with the httpRequest
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
            return new RetrySSEResult(e, event != null ? event.getId() : -1, event != null ? event.getRetryAfter() : null);
        }
        return null;
    }

    private boolean isEndOfBlock(StringBuilder sb) {
        // blocks of data are separated by double newlines
        // add more end of blocks here if needed
        return sb.indexOf("\n\n") >= 0;
    }

    private ServerSentEvent processLines(String[] lines) {
        List<String> eventData = new ArrayList<>();
        ServerSentEvent event = new ServerSentEvent();

        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx == 0) {
                event.setComment(line.substring(1).trim());
                continue;
            }
            String field = line.substring(0, idx < 0 ? lines.length : idx).trim().toLowerCase();
            String value = idx < 0 ? "" : line.substring(idx + 1).trim();

            switch (field) {
                case "event":
                    event.setEvent(value);
                    break;
                case "data":
                    eventData.add(value);
                    break;
                case "id":
                    if (!value.isEmpty()) {
                        event.setId(Long.parseLong(value));
                    }
                    break;
                case "retry":
                    if (!value.isEmpty() && DIGITS_ONLY.matcher(value).matches()) {
                        event.setRetryAfter(Duration.ofMillis(Long.parseLong(value)));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid data received from server");
            }
        }

        event.setEvent(event.getEvent() == null ? DEFAULT_EVENT : event.getEvent());
        if (!eventData.isEmpty()) {
            event.setData(eventData);
        }

        return event;
    }

    /**
     * Retries the request if the listener allows it
     *
     * @param retrySSEResult  the result of the retry
     * @param listener The listener object attached with the httpRequest
     * @param httpRequest the HTTP Request being sent
     */
    private void retryExceptionForSSE(RetrySSEResult retrySSEResult, ServerSentEventListener listener, HttpRequest httpRequest) {
        if (Thread.currentThread().isInterrupted() || !listener.shouldRetry(retrySSEResult.getException(), retrySSEResult.getRetryAfter(), retrySSEResult.getLastEventId())) {
            listener.onError(retrySSEResult.getException());
            return;
        }

        if (retrySSEResult.getLastEventId() != -1) {
            httpRequest.getHeaders().add(HeaderName.fromString(LAST_EVENT_ID), String.valueOf(retrySSEResult.getLastEventId()));
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
    private static AccessibleByteArrayOutputStream getAccessibleByteArrayOutputStream(HttpURLConnection connection) throws IOException {
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

        public RetrySSEResult(IOException e, long lastEventId, Duration retryAfter) {
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

    private static class SocketClient {

        private static final String HTTP_VERSION = " HTTP/1.1";
        private static final SSLSocketFactory SSL_SOCKET_FACTORY = (SSLSocketFactory) SSLSocketFactory.getDefault();

        /**
         * Opens a socket connection, then writes the PATCH request across the connection and reads the response
         *
         * @param httpRequest The HTTP Request being sent
         * @return an instance of HttpUrlConnectionResponse
         * @throws ProtocolException If the protocol is not HTTP or HTTPS
         * @throws IOException If an I/O error occurs
         */
        public static DefaultHttpClientResponse sendPatchRequest(HttpRequest httpRequest) throws IOException {
            final URL requestUrl = httpRequest.getUrl();
            final String protocol = requestUrl.getProtocol();
            final String host = requestUrl.getHost();
            final int port = requestUrl.getPort();

            switch (protocol) {
                case "https":
                    try (SSLSocket socket = (SSLSocket) SSL_SOCKET_FACTORY.createSocket(host, port)) {
                        return doInputOutput(httpRequest, socket);
                    }

                case "http":
                    try (Socket socket = new Socket(host, port)) {
                        return doInputOutput(httpRequest, socket);
                    }

                default:
                    throw LOGGER.logThrowableAsWarning(
                        new ProtocolException("Only HTTP and HTTPS are supported by this client."));
            }
        }

        /**
         * Calls buildAndSend to send a String representation of the request across the output stream, then calls
         * buildResponse to get an instance of HttpUrlConnectionResponse from the input stream
         *
         * @param httpRequest The HTTP Request being sent
         * @param socket An instance of the SocketClient
         * @return an instance of HttpUrlConnectionResponse
         */
        @SuppressWarnings("deprecation")
        private static DefaultHttpClientResponse doInputOutput(HttpRequest httpRequest, Socket socket)
            throws IOException {
            httpRequest.setHeader(HeaderName.HOST, httpRequest.getUrl().getHost());
            if (!"keep-alive".equalsIgnoreCase(httpRequest.getHeaders().getValue(HeaderName.CONNECTION))) {
                httpRequest.setHeader(HeaderName.CONNECTION, "close");
            }

            try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream())) {

                buildAndSend(httpRequest, out);
                DefaultHttpClientResponse response = buildResponse(httpRequest, in);

                Header locationHeader = response.getHeaders().get(HeaderName.LOCATION);
                String redirectLocation = (locationHeader == null) ? null : locationHeader.getValue();

                if (redirectLocation != null) {
                    if (redirectLocation.startsWith("http")) {
                        httpRequest.setUrl(redirectLocation);
                    } else {
                        httpRequest.setUrl(new URL(httpRequest.getUrl(), redirectLocation));
                    }
                    return sendPatchRequest(httpRequest);
                }
                return response;
            }
        }

        /**
         * Converts an instance of HttpRequest to a String representation for sending over the output stream
         *
         * @param httpRequest The HTTP Request being sent
         * @param out output stream for writing the request
         * @throws IOException If an I/O error occurs
         */
        private static void buildAndSend(HttpRequest httpRequest, OutputStreamWriter out) throws IOException {
            final StringBuilder request = new StringBuilder();

            request.append("PATCH ").append(httpRequest.getUrl().getPath()).append(HTTP_VERSION).append("\r\n");

            if (httpRequest.getHeaders().getSize() > 0) {
                for (Header header : httpRequest.getHeaders()) {
                    header.getValuesList()
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
         * @param reader the input stream from the socket
         * @return an instance of HttpUrlConnectionResponse
         * @throws IOException If an I/O error occurs
         */
        private static DefaultHttpClientResponse buildResponse(HttpRequest httpRequest, BufferedReader reader)
            throws IOException {
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

            return new DefaultHttpClientResponse(httpRequest, statusCode, headers, body);
        }
    }
}
