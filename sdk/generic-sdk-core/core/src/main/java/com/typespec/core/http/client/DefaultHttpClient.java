// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.client;

import com.typespec.core.http.client.HttpClient;
import com.typespec.core.http.models.HttpHeaderName;
import com.typespec.core.http.models.HttpMethod;
import com.typespec.core.http.models.HttpRequest;
import com.typespec.core.http.models.HttpResponse;
import com.typespec.core.http.models.HttpProxyOptions;
import com.typespec.core.models.BinaryData;
import com.typespec.core.models.Context;
import com.typespec.core.models.Header;
import com.typespec.core.http.models.HttpHeaders;
import com.typespec.core.util.ClientLogger;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
public class DefaultHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultHttpClient.class);
    private final long connectionTimeout;
    private final long readTimeout;
    private final HttpProxyOptions httpProxyOptions;

    DefaultHttpClient(Duration connectionTimeout, Duration readTimeout, HttpProxyOptions httpProxyOptions) {
        this.connectionTimeout = connectionTimeout == null ? -1 : connectionTimeout.toMillis();
        this.readTimeout = readTimeout == null ? -1 : readTimeout.toMillis();

        this.httpProxyOptions = httpProxyOptions;
    }

    /**
     * Synchronously send the HttpRequest.
     *
     * @param httpRequest The HTTP request being sent
     * @param context The context of the request, for any additional changes
     * @return The HttpResponse object
     */
    @Override
    public HttpResponse send(HttpRequest httpRequest, Context context) {
        if (httpRequest.getHttpMethod() == HttpMethod.PATCH) {
            return sendPatchViaSocket(httpRequest);
        }
//        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        HttpURLConnection connection = connect(httpRequest);

        sendBody(httpRequest, null, connection);
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
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Open a connection based on the HttpRequest URL
     *
     * If a proxy is specified, the authorization type will default to 'Basic' unless Digest authentication is
     * specified in the 'Authorization' header.
     *
     * @param httpRequest The HTTP Request being sent
     * @return The HttpURLConnection object
     */
    private HttpURLConnection connect(HttpRequest httpRequest) {
        try {
            HttpURLConnection connection;
            URL url = httpRequest.getUrl();

            if (httpProxyOptions != null) {
                InetSocketAddress address = httpProxyOptions.getAddress();
                if (address != null) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
                    connection = (HttpURLConnection) url.openConnection(proxy);
                    if (httpProxyOptions.getUsername() != null && httpProxyOptions.getPassword() != null) {
                        String authString = httpProxyOptions.getUsername() + ":" + httpProxyOptions.getPassword();
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
//            for (Header header : httpRequest.getHeaders()) {
//                for (String value : header.getValues()) {
//                    connection.addRequestProperty(header.getName(), value);
//                }
//            }
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
     * @return This method does not return any value
     */
    private void sendBody(HttpRequest httpRequest, Object progressReporter, HttpURLConnection connection) {
        BinaryData binaryDataBody = httpRequest.getBody();

        if (binaryDataBody != null) {
            switch (httpRequest.getHttpMethod()) {
                case GET:
                case HEAD: {
                    return;
                }
                case OPTIONS:
                case TRACE:
                case CONNECT:
                case POST:
                case PUT:
                case DELETE: {
                    connection.setDoOutput(true);
                    byte[] bytes = binaryDataBody.toBytes();
                    try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()))) {
                        os.write(bytes);
                        os.flush();
                    } catch (IOException e) {
                        throw LOGGER.logThrowableAsError(new RuntimeException(e));
                    }
                    return;
                }
                default: {
                    throw LOGGER.logThrowableAsError(new IllegalStateException("Unknown HTTP Method:"
                        + httpRequest.getHttpMethod()));
                }
            }
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

            HttpHeaders responseHttpHeaders = new HttpHeaders();
            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                if (entry.getKey() != null) {
                    List<String> values = new ArrayList<>();
                    entry.getValue().forEach(v -> values.add(0, v));
                    for (String headerValue : values) {
                        responseHttpHeaders.add(HttpHeaderName.fromString(entry.getKey()), headerValue);
                    }
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (InputStream errorStream = connection.getErrorStream();
                 InputStream inputStream = (errorStream == null) ? connection.getInputStream() : errorStream) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

            return new DefaultHttpResponse(
                httpRequest,
                responseCode,
                responseHttpHeaders,
                BinaryData.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray()))
            );
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        } finally {
            connection.disconnect();
        }
    }
}
