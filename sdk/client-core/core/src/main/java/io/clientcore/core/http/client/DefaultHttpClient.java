// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.implementation.util.CoreUtils;
import io.clientcore.core.models.SocketConnection;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.ServerSentEventUtils;
import io.clientcore.core.util.ServerSentResult;
import io.clientcore.core.util.binarydata.BinaryData;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
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
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.clientcore.core.http.models.ContentType.APPLICATION_OCTET_STREAM;
import static io.clientcore.core.http.models.HttpHeaderName.CONTENT_LENGTH;
import static io.clientcore.core.http.models.HttpHeaderName.CONTENT_TYPE;
import static io.clientcore.core.http.models.HttpMethod.HEAD;
import static io.clientcore.core.http.models.ResponseBodyMode.BUFFER;
import static io.clientcore.core.http.models.ResponseBodyMode.IGNORE;
import static io.clientcore.core.http.models.ResponseBodyMode.STREAM;
import static io.clientcore.core.util.ServerSentEventUtils.NO_LISTENER_ERROR_MESSAGE;
import static io.clientcore.core.util.ServerSentEventUtils.attemptRetry;
import static io.clientcore.core.util.ServerSentEventUtils.processTextEventStream;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
class DefaultHttpClient implements HttpClient {
    // Implementation notes:
    // Do not use HttpURLConnection.disconnect, rather use InputStream.close. Disconnect may close the Socket connection
    // in keep alive scenarios which we don't want. InputStream.close may also close the Socket connection but it has
    // finer control on whether that will happen based on keep alive information.
    // In the scenario we receive an error response stream, keep alive won't be honored as the connection received an
    // error status, and it will be handled appropriately.

    private static final ClientLogger LOGGER = new ClientLogger(DefaultHttpClient.class);

    private final long connectionTimeout;
    private final long readTimeout;
    private final ProxyOptions proxyOptions;
    private final SSLSocketFactory sslSocketFactory;
    private static int maxConnections;
    private static boolean keepConnectionAlive = true;
    private final SocketConnectionCache socketConnectionCache;

    DefaultHttpClient(Duration connectionTimeout, Duration readTimeout, ProxyOptions proxyOptions,
        SSLSocketFactory sslSocketFactory) {
        this.connectionTimeout = connectionTimeout == null ? -1 : connectionTimeout.toMillis();
        this.readTimeout = readTimeout == null ? -1 : readTimeout.toMillis();
        this.proxyOptions = proxyOptions;
        this.sslSocketFactory = sslSocketFactory;
        String keepAlive = System.getProperty("http.keepAlive");
        if (keepAlive != null && !Boolean.parseBoolean(keepAlive)) {
            keepConnectionAlive = false;
        }
        String maxConnectionsString = System.getProperty("http.maxConnections");
        maxConnections = maxConnectionsString != null
            ? Integer.parseInt(maxConnectionsString)
            : keepConnectionAlive ? 5 : 0;
        this.socketConnectionCache = SocketConnectionCache.getInstance(keepConnectionAlive, maxConnections,
            readTimeout == null ? -1 : (int) readTimeout.toMillis());
    }

    @Override
    public Response<?> send(HttpRequest httpRequest) throws IOException {
        SocketConnection socketConnection = null;
            if (httpRequest.getHttpMethod() == HttpMethod.PATCH) {
                final URL requestUrl = httpRequest.getUrl();
                final String host = requestUrl.getHost();
                final int port = requestUrl.getPort();

                socketConnection = socketConnectionCache.get(
                    new SocketConnection.SocketConnectionProperties(requestUrl, host, String.valueOf(port)));

                Response<?> response
                    = SocketClient.sendPatchRequest(httpRequest, socketConnection.getSocketInputStream(),
                    socketConnection.getSocketOutputStream());

                // Handle connection reusing
                socketConnectionCache.reuseConnection(socketConnection);
                return response;
        } else {
                HttpURLConnection urlConnection = connect(httpRequest);
                sendBody(httpRequest, urlConnection);
                return receiveResponse(httpRequest, urlConnection);
            }
    }

    private SSLSocketFactory getSslSocketFactory() {
        return (sslSocketFactory != null) ? sslSocketFactory : (SSLSocketFactory) SSLSocketFactory.getDefault();
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
    private HttpURLConnection connect(HttpRequest httpRequest) throws IOException {
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

        if (connection instanceof HttpsURLConnection && sslSocketFactory != null) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            httpsConnection.setSSLSocketFactory(sslSocketFactory);
        }

        if (connectionTimeout != -1) {
            connection.setConnectTimeout((int) connectionTimeout);
        }

        if (readTimeout != -1) {
            connection.setReadTimeout((int) readTimeout);
        }

        if (keepConnectionAlive) {
            connection.setRequestProperty(HttpHeaderName.CONNECTION.toString(), "keep-alive");
        }

        if (maxConnections > 0) {
            connection.setRequestProperty(HttpHeaderName.CONNECTION.toString(), "keep-alive");
            connection.setRequestProperty(HttpHeaderName.KEEP_ALIVE.toString(), "max=" + maxConnections);
        }

        connection.setRequestMethod(httpRequest.getHttpMethod().toString());

        for (HttpHeader header : httpRequest.getHeaders()) {
            for (String value : header.getValues()) {
                connection.addRequestProperty(header.getName().toString(), value);
            }
        }

        return connection;
    }

    /**
     * Synchronously sends the content of an HttpRequest via an HttpUrlConnection instance.
     *
     * @param httpRequest The HTTP Request being sent
     * @param connection The HttpURLConnection that is being sent to
     */
    private static void sendBody(HttpRequest httpRequest, HttpURLConnection connection) throws IOException {
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
     * @return A Response object
     * @throws IOException If an I/O error occurs
     * @throws RuntimeException If the ServerSentEventListener is not set
     */
    private Response<?> receiveResponse(HttpRequest httpRequest, HttpURLConnection connection) throws IOException {
        HttpHeaders responseHeaders = getResponseHeaders(connection);
        HttpResponse<?> httpResponse = createHttpResponse(httpRequest, connection);
        RequestOptions options = httpRequest.getRequestOptions();
        ServerSentResult serverSentResult = null;

        // First check if we've gotten back an error response. If so, handle it now and ignore everything else.
        if (connection.getErrorStream() != null) {
            // Read the error stream to completion to ensure the connection is released back to the pool and set it as
            // the response body.
            eagerlyBufferResponseBody(httpResponse, connection.getErrorStream());
            return httpResponse;
        }

        if (isTextEventStream(responseHeaders)) {
            ServerSentEventListener listener = httpRequest.getServerSentEventListener();

            if (listener == null) {
                connection.getInputStream().close();
                throw LOGGER.logThrowableAsError(new RuntimeException(NO_LISTENER_ERROR_MESSAGE));
            }

            serverSentResult = processTextEventStream(connection.getInputStream(), listener);

            if (serverSentResult.getException() != null) {
                // If an exception occurred while processing the text event stream, emit listener onError.
                connection.getInputStream().close();
                listener.onError(serverSentResult.getException());
            }

            // If an error occurred or we want to reconnect
            if (!Thread.currentThread().isInterrupted() && attemptRetry(serverSentResult, httpRequest)) {
                return this.send(httpRequest);
            }
            // If no error occurred and we don't want to reconnect, continue response body handling.
        }

        ResponseBodyMode responseBodyMode = null;

        if (options != null) {
            responseBodyMode = options.getResponseBodyMode();
        }

        if (responseBodyMode == null) {
            responseBodyMode = determineResponseBodyMode(httpRequest, httpResponse.getHeaders());
        }

        switch (responseBodyMode) {
            case IGNORE:
                HttpResponseAccessHelper.setBody(httpResponse, BinaryData.EMPTY);

                // Close the response InputStream rather than using disconnect. Disconnect will close the Socket
                // connection when the InputStream is still open, which can result in keep alive handling not being
                // applied.
                connection.getInputStream().close();

                break;
            case STREAM:
                if (isTextEventStream(responseHeaders)) {
                    HttpResponseAccessHelper.setBody(httpResponse, createBodyFromServerSentResult(serverSentResult));
                } else {
                    streamResponseBody(httpResponse, connection);
                }
                break;
            case BUFFER:
            case DESERIALIZE:
                // Deserialization will occur at a later point in HttpResponseBodyDecoder.
                if (isTextEventStream(responseHeaders)) {
                    HttpResponseAccessHelper.setBody(httpResponse, createBodyFromServerSentResult(serverSentResult));
                } else {
                    eagerlyBufferResponseBody(httpResponse, connection.getInputStream());
                }
                break;
            default:
                eagerlyBufferResponseBody(httpResponse, connection.getInputStream());
                break;
        }
        return httpResponse;
    }

    private static HttpResponse<?> createHttpResponse(HttpRequest httpRequest, HttpURLConnection connection)
        throws IOException {
        return new HttpResponse<>(httpRequest, connection.getResponseCode(), getResponseHeaders(connection), null);
    }

    private static boolean isTextEventStream(HttpHeaders responseHeaders) {
        if (responseHeaders != null) {
            return ServerSentEventUtils.isTextEventStreamContentType(responseHeaders.getValue(CONTENT_TYPE));
        }

        return false;
    }

    private BinaryData createBodyFromServerSentResult(ServerSentResult serverSentResult) {
        String bodyContent = (serverSentResult != null && serverSentResult.getData() != null)
            ? String.join("\n", serverSentResult.getData())
            : "";
        return BinaryData.fromString(bodyContent);
    }

    private static void streamResponseBody(HttpResponse<?> httpResponse, HttpURLConnection connection)
        throws IOException {

        HttpResponseAccessHelper.setBody(httpResponse, BinaryData.fromStream(connection.getInputStream()));
    }

    private static void eagerlyBufferResponseBody(HttpResponse<?> httpResponse, InputStream stream)
        throws IOException {
        int contentLength = speculateContentLength(httpResponse.getHeaders());
        AccessibleByteArrayOutputStream outputStream = getAccessibleByteArrayOutputStream(stream, contentLength);

        HttpResponseAccessHelper.setBody(httpResponse, BinaryData.fromByteBuffer(outputStream.toByteBuffer()));
    }

    private static HttpHeaders getResponseHeaders(HttpURLConnection connection) {
        Map<String, List<String>> hucHeaders = connection.getHeaderFields();
        HttpHeaders responseHeaders = new HttpHeaders(hucHeaders.size());
        for (Map.Entry<String, List<String>> entry : hucHeaders.entrySet()) {
            if (entry.getKey() != null) {
                responseHeaders.add(HttpHeaderName.fromString(entry.getKey()), entry.getValue());
            }
        }
        return responseHeaders;
    }

    private static AccessibleByteArrayOutputStream getAccessibleByteArrayOutputStream(InputStream stream,
        int contentLength) throws IOException {
        AccessibleByteArrayOutputStream outputStream = (contentLength >= 0)
            ? new AccessibleByteArrayOutputStream(contentLength)
            : new AccessibleByteArrayOutputStream();

        try (InputStream inputStream = stream) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        }
        return outputStream;
    }

    private static int speculateContentLength(HttpHeaders headers) {
        String contentLength = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (contentLength == null) {
            return -1;
        }

        // We're only speculating an integer sized Content-Length. If it's not an integer, or larger than an integer,
        // we'll return -1 to indicate that we don't know the content length.
        try {
            return Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    private ResponseBodyMode determineResponseBodyMode(HttpRequest httpRequest, HttpHeaders responseHeaders) {
        HttpHeader contentType = responseHeaders.get(CONTENT_TYPE);

        if (httpRequest.getHttpMethod() == HEAD) {
            return IGNORE;
        } else if (contentType != null && APPLICATION_OCTET_STREAM
            .regionMatches(true, 0, contentType.getValue(), 0, APPLICATION_OCTET_STREAM.length())) {

            return STREAM;
        } else {
            return BUFFER;
        }
    }

    /*
     * Inner class maintaining a cache of socket connections
     */
    private static class SocketConnectionCache {
        private static SocketConnectionCache INSTANCE;
        private final int maxConnections;
        private final Map<SocketConnection.SocketConnectionProperties, List<SocketConnection>> connectionPool
            = new HashMap<SocketConnection.SocketConnectionProperties, List<SocketConnection>>();
        private final int connectionTimeout;

        private SocketConnectionCache(boolean connectionKeepAlive, int maximumConnections, int connectionTimeout) {
            if (!connectionKeepAlive) {
                maxConnections = 0;
            } else {
                this.maxConnections = maximumConnections;
            }
            this.connectionTimeout = connectionTimeout;
        }

        public static synchronized SocketConnectionCache getInstance(boolean connectionKeepAlive, int maximumConnections, int connectionTimeout) {
            if (INSTANCE == null) {
                INSTANCE = new SocketConnectionCache(connectionKeepAlive, maximumConnections, connectionTimeout);
            }
            return INSTANCE;
        }


        private SocketConnection get(SocketConnection.SocketConnectionProperties socketConnectionProperties) throws SocketException {
            SocketConnection connection = null;
            // Try-Get a connection from the cache
            synchronized (connectionPool) {
                List<SocketConnection> connections = connectionPool.get(socketConnectionProperties);
                while (!CoreUtils.isNullOrEmpty(connections)) {
                    connection = connections.remove(connections.size() - 1);
                    if (connections.isEmpty()) {
                        connectionPool.remove(socketConnectionProperties);
                        connections = null;
                    } else {
                        connectionPool.put(socketConnectionProperties, connections);
                    }
                    // keep removing connections from list until we find a use-able one, disregard other connections in use
                    if (connection.canBeReused()) {
                        return connection;
                    }
                }
            }

            // If no connection is available, create a new one
            if (connection == null) {
                // If the request is a PATCH request, we need to use a socket connection
                connection = getSocketSocketConnection(socketConnectionProperties, connectionTimeout);
            }

            synchronized (connectionPool) {
                List<SocketConnection> connections = connectionPool.get(socketConnectionProperties);
                if (connections == null) {
                    connections = new ArrayList<>();
                    connectionPool.put(socketConnectionProperties, connections);
                }
            }
            return connection;
        }

        private void reuseConnection(SocketConnection connection) throws IOException {

            if (maxConnections > 0 && connection.canBeReused()) {
                SocketConnection.SocketConnectionProperties connectionProperties = connection.getConnectionProperties();
                InputStream in = connection.getSocketInputStream();
                synchronized (connectionPool) {
                    List<SocketConnection> connections = connectionPool.get(connectionProperties);
                    if (connections == null) {
                        connections = new ArrayList<SocketConnection>();
                        connectionPool.put(connectionProperties, connections);
                    }
                    if (connections.size() < maxConnections) {
                        if (keepConnectionAlive) {
                            do {
                                in.skip(Long.MAX_VALUE);
                            } while (in.read() != -1);
                            // mark the connection as available for reuse
                            connection.markAvailableForReuse();
                            connections.add(connection);
                            connectionPool.put(connectionProperties, connections);
                        }
                    }
                    return; // keep the connection open
                }
            }

            // close streams when connection cannot be reused.
            connection.closeSocketAndStreams();
        }

        private static SocketConnection getSocketSocketConnection(SocketConnection.SocketConnectionProperties socketConnectionProperties, int connectionTimeout) throws SocketException {
            SocketConnection connection;
            URL requestUrl = socketConnectionProperties.getHttpRequest().getUrl();
            String protocol = requestUrl.getProtocol();
            String host = requestUrl.getHost();
            int port = requestUrl.getPort();
            Socket socket;
            try {
                socket = protocol.equals("https")
                    ? (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port)
                    : new Socket(host, port);
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }
            // socket.setSoTimeout(connectionTimeout);
            connection = new SocketConnection(socket, socketConnectionProperties);

            return connection;
        }
    }

    private static class SocketClient {

        private static final String HTTP_VERSION = " HTTP/1.1";

        /**
         * Calls buildAndSend to send a String representation of the request across the output stream, then calls
         * buildResponse to get an instance of HttpUrlConnectionResponse from the input stream
         *
         * @param httpRequest  The HTTP Request being sent
         * @param bufferedInputStream  the input stream from the socket
         * @param outputStream the output stream from the socket for writing the request
         * @return an instance of Response
         */
        private static Response<?> sendPatchRequest(HttpRequest httpRequest, BufferedInputStream bufferedInputStream, OutputStream outputStream) throws IOException {
            httpRequest.getHeaders().set(HttpHeaderName.HOST, httpRequest.getUrl().getHost());
            BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8));
            OutputStreamWriter out = new OutputStreamWriter(outputStream);

            buildAndSend(httpRequest, out);
            Response<?> response = buildResponse(httpRequest, reader);

            HttpHeader locationHeader = response.getHeaders().get(HttpHeaderName.LOCATION);
            String redirectLocation = (locationHeader == null) ? null : locationHeader.getValue();

            if (redirectLocation != null) {
                if (redirectLocation.startsWith("http")) {
                    httpRequest.setUrl(redirectLocation);
                } else {
                    httpRequest.setUrl(new URL(httpRequest.getUrl(), redirectLocation));
                }
                return sendPatchRequest(httpRequest, bufferedInputStream, outputStream);
            }

            return response;

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
                for (HttpHeader header : httpRequest.getHeaders()) {
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
         * Response
         *
         * @param httpRequest The HTTP Request being sent
         * @param reader the input stream from the socket
         * @return an instance of Response
         * @throws IOException If an I/O error occurs
         */
        private static Response<?> buildResponse(HttpRequest httpRequest, BufferedReader reader) throws IOException {
            // Parse Http response from socket:
            // Status Line
            // Response Headers
            // Blank Line
            // Response Body

            int statusCode = readStatusCode(reader);

            HttpHeaders headers = readResponseHeaders(reader);
            HttpHeader contentLengthHeader = headers.get(CONTENT_LENGTH);
            String line;
            // read body if present
            if (headers.getSize() != 0 && contentLengthHeader != null && contentLengthHeader.getValue() != null) {
                StringBuilder bodyString = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    bodyString.append(line);
                }

                return new HttpResponse<>(httpRequest, statusCode, headers,
                    BinaryData.fromString(bodyString.toString()));
            }
            return new HttpResponse<>(httpRequest, statusCode, headers, null);
        }

        private static int readStatusCode(BufferedReader reader) throws IOException {

            while(!reader.ready()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            String statusLine = reader.readLine();

            if (statusLine == null) {
                throw LOGGER.logThrowableAsError(new IllegalStateException("Unexpected response from server."));
            }
            int dotIndex = statusLine.indexOf('.');
            return Integer.parseInt(statusLine.substring(dotIndex + 3, dotIndex + 6));
        }

        private static HttpHeaders readResponseHeaders(BufferedReader reader) throws IOException {
            HttpHeaders headers = new HttpHeaders();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Headers may have optional leading and trailing whitespace around the header value.
                // https://tools.ietf.org/html/rfc7230#section-3.2
                // Process this accordingly.
                int split = line.indexOf(':'); // Find ':' to split the header name and value.
                String key = line.substring(0, split); // Get the header name.
                String value = line.substring(split + 1).trim(); // Get the header value and trim whitespace.
                headers.add(HttpHeaderName.fromString(key), value);
            }
            return headers;
        }
    }
}
