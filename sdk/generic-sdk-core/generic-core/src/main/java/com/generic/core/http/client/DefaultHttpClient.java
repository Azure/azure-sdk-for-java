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
    public static final String LAST_EVENT_ID = "Last-Event-Id";
    private final long connectionTimeout;
    private final long readTimeout;
    private final ProxyOptions proxyOptions;
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
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
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
                    throw LOGGER.logThrowableAsError(new RuntimeException(e));
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
            RetrySSEResult retrySSEResult;
            if (connection.getErrorStream() == null) {
                if (isTextEventStream(responseHeaders) && listener != null) {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        retrySSEResult = processBuffer(reader, listener);
                        if (retrySSEResult != null) {
                            retryExceptionForSSE(retrySSEResult, listener, httpRequest);
                        }
                    } catch (IOException e) {
                        throw LOGGER.logThrowableAsError(new RuntimeException(e));
                    }
                }
                return new DefaultHttpClientResponse(httpRequest, responseCode, responseHeaders);
            } else {
                AccessibleByteArrayOutputStream outputStream = getAccessibleByteArrayOutputStream(connection);
                return new DefaultHttpClientResponse(httpRequest, responseCode, responseHeaders,
                    BinaryData.fromByteBuffer(outputStream.toByteBuffer()));
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        } finally {
            connection.disconnect();
        }
    }

    private Headers getResponseHeaders(HttpURLConnection connection) {
        Map<String, List<String>> hucHeaders = connection.getHeaderFields();
        Headers responseHeaders = new Headers((int) (hucHeaders.size() / 0.75F));
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            if (entry.getKey() != null) {
                responseHeaders.add(HeaderName.fromString(entry.getKey()), entry.getValue());
            }
        }
        return responseHeaders;
    }

    private boolean isTextEventStream(Headers responseHeaders) {
        return responseHeaders.get(HeaderName.CONTENT_TYPE).getValue().equals(ContentType.TEXT_EVENT_STREAM);
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
     * Processes the sse buffer and dispatches the event
     *
     * @param reader   The BufferedReader object
     * @param listener The listener object attached with the httpRequest
     */
    private RetrySSEResult processBuffer(BufferedReader reader, ServerSentEventListener listener) {
        StringBuilder collectedData = new StringBuilder();
        ServerSentEvent event = null;
        try {
            int dataRead = reader.read();
            while (dataRead != -1) {
                collectedData.append((char) dataRead);
                dataRead = reader.read();
                int index = isEndOfBlock(collectedData);
                if (index >= 0) {
                    String[] lines = collectedData.substring(0, index).split("\n"); // split the block into lines
                    collectedData.delete(0, index + 2); // clear first block including "\n\n"
                    event = processLines(lines);
                    if (event != null
                        && (!Objects.equals(event.getEvent(), DEFAULT_EVENT) || event.getData() != null)) {
                        // dispatch the event if there is data or event
                        listener.onEvent(event);
                    }
                }
            }
            listener.onClose();
        } catch (IOException e) {
            return new RetrySSEResult(e, event.getId(), event.getRetryAfter());
        }
        return null;
    }

    private int isEndOfBlock(StringBuilder sb) {
        // blocks of data are separated by double newlines
        return sb.indexOf("\n\n");
        // confirm the ways to check for end of block
    }

    private ServerSentEvent processLines(String[] lines) {
        StringBuilder eventData = new StringBuilder();
        ServerSentEvent event = new ServerSentEvent();

        //start parsing line by line
        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx < 0) {
                continue; // ignore invalid data
            } else if (idx == 0) {
                // capture comment line
                event.setComment(line.substring(1).trim());
                continue;
            }
            String field = line.substring(0, idx);
            String value = line.substring(idx + 1).trim();
            switch (field.trim().toLowerCase()) {
                case "event":
                    event.setEvent(value);
                    continue;

                case "data":
                    if (eventData.length() > 0) {
                        eventData.append("\n");
                    }
                    eventData.append(value);
                    continue;

                case "id":
                    if (!value.isEmpty()) {
                        event.setId(Long.parseLong(value));
                    }
                    continue;

                case "retry":
                    if (!value.isEmpty() && DIGITS_ONLY.matcher(value).matches()) {
                        event.setRetryAfter(Long.parseLong(value));
                    }
                    continue;

                default:
                    continue;
            }
        }
        if (event.getEvent() == null) {
            event.setEvent(DEFAULT_EVENT);
        }
        if (eventData.length() != 0) {
            event = event.setData(eventData.toString());
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
        long lastEventId = retrySSEResult.getLastEventId();
        long retryAfter = retrySSEResult.getRetryAfter();
        if (!Thread.currentThread().isInterrupted() && listener.shouldRetry(retrySSEResult.getException(), retryAfter, lastEventId)) {
            if (lastEventId != -1) {
                httpRequest.getHeaders().add(HeaderName.fromString(LAST_EVENT_ID),
                    String.valueOf(lastEventId));
            }
            try {
                if (retryAfter > 0) {
                    Thread.sleep(retryAfter);
                }
            } catch (InterruptedException ignored) {
                return;
            }
            if (!Thread.currentThread().isInterrupted()) {
                this.send(httpRequest);
            }
        } else {
            listener.onError(retrySSEResult.getException());
        }
    }

    /**
     * Inner class to hold the result for a retry of an SSE request
     */
    private static class RetrySSEResult {
        private final long lastEventId;
        private final long retryAfter;
        private final IOException ioException;

        public RetrySSEResult(IOException e, long lastEventId, long retryAfter) {
            this.ioException = e;
            this.lastEventId = lastEventId;
            this.retryAfter = retryAfter;
        }

        public long getLastEventId() {
            return lastEventId;
        }

        public long getRetryAfter() {
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
         * Opens a socket connection, then writes the PATCH request across the
         * connection and reads the response
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
         * Calls buildAndSend to send a String representation of the request across the output
         * stream, then calls buildResponse to get an instance of HttpUrlConnectionResponse
         * from the input stream
         *
         * @param httpRequest The HTTP Request being sent
         * @param socket An instance of the SocketClient
         * @return an instance of HttpUrlConnectionResponse
         */
        @SuppressWarnings("deprecation")
        private static DefaultHttpClientResponse doInputOutput(HttpRequest httpRequest, Socket socket) throws IOException {
            httpRequest.setHeader(HeaderName.HOST, httpRequest.getUrl().getHost());
            if (!"keep-alive".equalsIgnoreCase(httpRequest.getHeaders().getValue(HeaderName.CONNECTION))) {
                httpRequest.setHeader(HeaderName.CONNECTION, "close");
            }

            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
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
         * Converts an instance of HttpRequest to a String representation for sending
         * over the output stream
         *
         * @param httpRequest The HTTP Request being sent
         * @param out output stream for writing the request
         * @throws IOException If an I/O error occurs
         */
        private static void buildAndSend(HttpRequest httpRequest, OutputStreamWriter out) throws IOException {
            final StringBuilder request = new StringBuilder();

            request.append("PATCH ")
                .append(httpRequest.getUrl().getPath())
                .append(HTTP_VERSION)
                .append("\r\n");

            if (httpRequest.getHeaders().getSize() > 0) {
                for (Header header : httpRequest.getHeaders()) {
                    header.getValuesList().forEach(value -> request.append(header.getName())
                        .append(": ")
                        .append(value)
                        .append("\r\n"));
                }
            }
            if (httpRequest.getBody() != null) {
                request.append("\r\n")
                    .append(httpRequest.getBody().toString())
                    .append("\r\n");
            }

            out.write(request.toString());
            out.flush();
        }

        /**
         * Reads the response from the input stream and extracts the information
         * needed to construct an instance of HttpUrlConnectionResponse
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
                String[] kv = line.split(": ", 2);
                String k = kv[0];
                String v = kv[1];
                headers.add(HeaderName.fromString(k), v);
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
