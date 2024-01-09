// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.ProxyOptions;
import com.generic.core.implementation.AccessibleByteArrayOutputStream;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
class DefaultHttpClient implements HttpClient {
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
     *
     * @return The HttpResponse object
     */
    @Override
    public HttpResponse send(HttpRequest httpRequest) {
        if (httpRequest.getHttpMethod() == HttpMethod.PATCH) {
            return sendPatchViaSocket(httpRequest);
        }

        HttpURLConnection connection = connect(httpRequest);

        sendBody(httpRequest, null, connection);

        return receiveResponse(httpRequest, connection);
    }

    /**
     * Synchronously sends a PATCH request via a socket client.
     *
     * @param httpRequest The HTTP request being sent
     *
     * @return The HttpResponse object
     */
    private HttpResponse sendPatchViaSocket(HttpRequest httpRequest) {
        try {
            return SocketClient.sendPatchRequest(httpRequest);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Open a connection based on the HttpRequest URL
     *
     * <p>If a proxy is specified, the authorization type will default to 'Basic' unless Digest authentication is
     * specified in the 'Authorization' header.</p>
     *
     * @param httpRequest The HTTP Request being sent
     *
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
                    throw new ConnectException("Invalid proxy address");
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
     * @param progressReporter A reporter for the progress of the request
     * @param connection The HttpURLConnection that is being sent to
     */
    private void sendBody(HttpRequest httpRequest, Object progressReporter, HttpURLConnection connection) {
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
     *
     * @return A HttpResponse object
     */
    private HttpResponse receiveResponse(HttpRequest httpRequest, HttpURLConnection connection) {
        try {
            int responseCode = connection.getResponseCode();

            Map<String, List<String>> hucHeaders = connection.getHeaderFields();
            Headers responseHeaders = new Headers((int) (hucHeaders.size() / 0.75F));

            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                if (entry.getKey() != null) {
                    responseHeaders.add(HeaderName.fromString(entry.getKey()), entry.getValue());
                }
            }

            AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();

            try (InputStream errorStream = connection.getErrorStream();
                 InputStream inputStream = (errorStream == null) ? connection.getInputStream() : errorStream) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

            return new DefaultHttpClientResponse(httpRequest, responseCode, responseHeaders,
                BinaryData.fromByteBuffer(outputStream.toByteBuffer()));
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
        // no need to disconnect, connections can and should be reused.
        // The caller is responsible for closing the response which will close underlying stream.
    }

    private static class SocketClient {

        private static final String HTTP_VERSION = " HTTP/1.1";
        private static final SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        /**
         * Opens a socket connection, then writes the PATCH request across the
         * connection and reads the response
         *
         * @param httpRequest The HTTP Request being sent
         * @return an instance of HttpUrlConnectionResponse
         */
        public static DefaultHttpClientResponse sendPatchRequest(HttpRequest httpRequest) throws IOException {
            final URL requestUrl = httpRequest.getUrl();
            final String protocol = requestUrl.getProtocol();
            final String host = requestUrl.getHost();
            final int port = requestUrl.getPort();

            switch (protocol) {
                case "https": {
                    try (SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port)) {
                        return doInputOutput(httpRequest, socket);
                    }
                }
                case "http": {
                    try (Socket socket = new Socket(host, port)) {
                        return doInputOutput(httpRequest, socket);
                    }
                }
            }
            throw new ProtocolException("Only HTTP and HTTPS are supported by this client.");
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
         */
        private static void buildAndSend(HttpRequest httpRequest, OutputStreamWriter out) throws IOException {
            final StringBuilder request = new StringBuilder();

            request.append("PATCH")
                .append(" ")
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
         */
        private static DefaultHttpClientResponse buildResponse(HttpRequest httpRequest, BufferedReader reader) throws IOException {
            String statusLine = reader.readLine();
            int dotIndex = statusLine.indexOf('.');
            int statusCode = Integer.parseInt(statusLine.substring(dotIndex+3, dotIndex+6));

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

            BinaryData body = BinaryData.fromBytes(bodyString.toString().getBytes());

            return new DefaultHttpClientResponse(httpRequest, statusCode, headers, body);
        }
    }
}
