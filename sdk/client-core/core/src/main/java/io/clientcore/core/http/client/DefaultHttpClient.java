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
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.implementation.util.ServerSentEventUtil;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;

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

import static io.clientcore.core.http.models.ContentType.APPLICATION_OCTET_STREAM;
import static io.clientcore.core.http.models.HttpHeaderName.CONTENT_TYPE;
import static io.clientcore.core.http.models.ResponseBodyMode.BUFFER;
import static io.clientcore.core.http.models.ResponseBodyMode.STREAM;
import static io.clientcore.core.implementation.util.ServerSentEventUtil.NO_LISTENER_ERROR_MESSAGE;
import static io.clientcore.core.implementation.util.ServerSentEventUtil.processTextEventStream;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
class DefaultHttpClient implements HttpClient {
    private static final BinaryData EMPTY_BODY = BinaryData.fromBytes(new byte[0]);
    private static final ClientLogger LOGGER = new ClientLogger(DefaultHttpClient.class);

    private final long connectionTimeout;
    private final long readTimeout;
    private final ProxyOptions proxyOptions;

    DefaultHttpClient(Duration connectionTimeout, Duration readTimeout, ProxyOptions proxyOptions) {
        this.connectionTimeout = connectionTimeout == null ? -1 : connectionTimeout.toMillis();
        this.readTimeout = readTimeout == null ? -1 : readTimeout.toMillis();
        this.proxyOptions = proxyOptions;
    }

    /**
     * Synchronously send the HttpRequest.
     *
     * @param httpRequest The HTTP request being sent
     * @return The Response object
     */
    @Override
    public Response<?> send(HttpRequest httpRequest) {
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
     * @return The Response object
     */
    private Response<?> sendPatchViaSocket(HttpRequest httpRequest) {
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

            for (HttpHeader header : httpRequest.getHeaders()) {
                for (String value : header.getValues()) {
                    connection.addRequestProperty(header.getName().toString(), value);
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
    private Response<?> receiveResponse(HttpRequest httpRequest, HttpURLConnection connection) {
        HttpHeaders responseHeaders = getResponseHeaders(connection);
        HttpResponse<?> httpResponse = createHttpResponse(httpRequest, connection);

        if (isTextEventStream(responseHeaders)) {
            try {
                ServerSentEventListener listener = httpRequest.getServerSentEventListener();

                if (listener == null) {
                    throw LOGGER.logThrowableAsError(new RuntimeException(NO_LISTENER_ERROR_MESSAGE));
                }

                if (connection.getErrorStream() == null) {
                    processTextEventStream(httpRequest, httpRequestConsumer ->
                        this.send(httpRequest), connection.getInputStream(), listener, LOGGER);
                }
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            } finally {
                connection.disconnect();
            }
        } else {
            ResponseBodyMode responseBodyMode = httpRequest.getRequestOptions().getResponseBodyMode();

            if (responseBodyMode == null) {
                HttpHeader contentType = httpResponse.getHeaders().get(CONTENT_TYPE);

                if (contentType != null && APPLICATION_OCTET_STREAM.regionMatches(true, 0, contentType.getValue(), 0,
                    APPLICATION_OCTET_STREAM.length())) {

                    responseBodyMode = STREAM;
                } else {
                    responseBodyMode = BUFFER;
                }

                httpRequest.getRequestOptions().setResponseBodyMode(responseBodyMode); // We only change this if it was null.
            }

            switch (responseBodyMode) {
                case IGNORE:
                    HttpResponseAccessHelper.setBody(httpResponse, EMPTY_BODY);

                    connection.disconnect();

                    break;
                case STREAM:
                    streamResponseBody(httpResponse, connection);

                    break;
                case BUFFER:
                case DESERIALIZE: // Deserialization will occur at a later point in HttpResponseBodyDecoder.
                default:
                    eagerlyBufferResponseBody(httpResponse, connection);
            }
        }

        return httpResponse;
    }

    private HttpResponse<?> createHttpResponse(HttpRequest httpRequest, HttpURLConnection connection) {
        try {
            return new HttpResponse<>(httpRequest, connection.getResponseCode(), getResponseHeaders(connection), null);
        } catch (IOException e) {
            connection.disconnect();

            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private static boolean isTextEventStream(HttpHeaders responseHeaders) {
        if (responseHeaders != null) {
            return ServerSentEventUtil.isTextEventStreamContentType(responseHeaders.getValue(CONTENT_TYPE));
        }
        return false;
    }

    private void streamResponseBody(HttpResponse<?> httpResponse, HttpURLConnection connection) {
        try {
            HttpResponseAccessHelper.setBody(httpResponse, BinaryData.fromStream(connection.getInputStream()));
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private void eagerlyBufferResponseBody(HttpResponse<?> httpResponse, HttpURLConnection connection) {
        try {
            AccessibleByteArrayOutputStream outputStream = getAccessibleByteArrayOutputStream(connection);

            HttpResponseAccessHelper.setBody(httpResponse, BinaryData.fromByteBuffer(outputStream.toByteBuffer()));
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } finally {
            connection.disconnect();
        }
    }

    private HttpHeaders getResponseHeaders(HttpURLConnection connection) {
        Map<String, List<String>> hucHeaders = connection.getHeaderFields();
        HttpHeaders responseHeaders = new HttpHeaders(hucHeaders.size());
        for (Map.Entry<String, List<String>> entry : hucHeaders.entrySet()) {
            if (entry.getKey() != null) {
                responseHeaders.add(HttpHeaderName.fromString(entry.getKey()), entry.getValue());
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
        public static Response<?> sendPatchRequest(HttpRequest httpRequest) throws IOException {
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
         * @return an instance of Response
         */
        @SuppressWarnings("deprecation")
        private static Response<?> doInputOutput(HttpRequest httpRequest, Socket socket) throws IOException {
            httpRequest.getHeaders().set(HttpHeaderName.HOST, httpRequest.getUrl().getHost());
            if (!"keep-alive".equalsIgnoreCase(httpRequest.getHeaders().getValue(HttpHeaderName.CONNECTION))) {
                httpRequest.getHeaders().set(HttpHeaderName.CONNECTION, "close");
            }

            try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream())) {

                buildAndSend(httpRequest, out);

                Response<?> response = buildResponse(httpRequest, in);
                HttpHeader locationHeader = response.getHeaders().get(HttpHeaderName.LOCATION);
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
         * HttpUrlConnectionResponse
         *
         * @param httpRequest The HTTP Request being sent
         * @param reader the input stream from the socket
         * @return an instance of HttpUrlConnectionResponse
         * @throws IOException If an I/O error occurs
         */
        private static Response<?> buildResponse(HttpRequest httpRequest, BufferedReader reader) throws IOException {
            String statusLine = reader.readLine();
            int dotIndex = statusLine.indexOf('.');
            int statusCode = Integer.parseInt(statusLine.substring(dotIndex + 3, dotIndex + 6));

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

            StringBuilder bodyString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                bodyString.append(line);
            }

            BinaryData body = BinaryData.fromByteBuffer(ByteBuffer.wrap(bodyString.toString().getBytes()));

            return new HttpResponse<>(httpRequest, statusCode, headers, body);
        }
    }
}
