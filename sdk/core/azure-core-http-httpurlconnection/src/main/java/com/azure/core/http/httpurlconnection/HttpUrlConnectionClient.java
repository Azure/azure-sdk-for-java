// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
public class HttpUrlConnectionClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(HttpUrlConnectionClient.class);
    private final long connectionTimeout;
    private final long readTimeout;
    private final Duration writeTimeout;
    private final Duration responseTimeout;
    private final ProxyOptions proxyOptions;

    HttpUrlConnectionClient(Duration connectionTimeout, Duration readTimeout, Duration writeTimeout,
                            Duration responseTimeout, ProxyOptions proxyOptions) {
        this.connectionTimeout = connectionTimeout == null ? -1 : connectionTimeout.toMillis();
        this.readTimeout = readTimeout == null ? -1 : readTimeout.toMillis();
        this.writeTimeout = writeTimeout;
        this.responseTimeout = responseTimeout;

        this.proxyOptions = proxyOptions;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest) {
        return send(httpRequest, Context.NONE);
    }

    /**
     * Asynchronously send the HttpRequest.
     *
     * @param httpRequest The HTTP request being sent
     * @param context The context of the request, for any additional changes
     * @return A Mono containing the HttpResponse object
     */
    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest, Context context) {
        if (httpRequest.getHttpMethod() == HttpMethod.PATCH) {
            return sendPatchViaSocket(httpRequest);
        }
        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        return Mono.defer(() -> {
            HttpURLConnection connection = connect(httpRequest);
            return sendBodyAsync(httpRequest, progressReporter, connection)
                .then(Mono.fromCallable(() -> receiveResponse(httpRequest, connection)))
                .timeout(responseTimeout)
                .publishOn(Schedulers.boundedElastic());
        });
    }

    /**
     * Synchronously send the HttpRequest.
     *
     * @param httpRequest The HTTP request being sent
     * @param context The context of the request, for any additional changes
     * @return The HttpResponse object
     */
    @Override
    public HttpResponse sendSync(HttpRequest httpRequest, Context context) {
        if (httpRequest.getHttpMethod() == HttpMethod.PATCH) {
            return sendPatchViaSocketSync(httpRequest);
        }
        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        HttpURLConnection connection = connect(httpRequest);
        sendBodySync(httpRequest, progressReporter, connection);
        return receiveResponse(httpRequest, connection);
    }

    /**
     * Asynchronously sends a PATCH request via a socket client.
     *
     * @param httpRequest The HTTP request being sent
     * @return A Mono containing the HttpResponse object
     */
    private Mono<HttpResponse> sendPatchViaSocket(HttpRequest httpRequest) {
        return Mono.fromCallable(() -> sendPatchViaSocketSync(httpRequest));
    }

    /**
     * Synchronously sends a PATCH request via a socket client.
     *
     * @param httpRequest The HTTP request being sent
     * @return The HttpResponse object
     */
    private HttpResponse sendPatchViaSocketSync(HttpRequest httpRequest) {
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

            if (proxyOptions != null) {
                InetSocketAddress address = proxyOptions.getAddress();
                if (address != null) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
                    connection = (HttpURLConnection) url.openConnection(proxy);
                    if (proxyOptions.getUsername() != null && proxyOptions.getPassword() != null) {
                        String token = httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
                        if (token != null && token.startsWith("Digest")) {
                            MessageDigest messageDigest = null;
                            try {
                                messageDigest = MessageDigest.getInstance("SHA-256");
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            String authString = proxyOptions.getUsername() + ":" + proxyOptions.getPassword();
                            assert messageDigest != null;
                            messageDigest.update(authString.getBytes());
                            String authStringEnc = Base64.getEncoder().encodeToString(messageDigest.digest());
                            connection.setRequestProperty("Proxy-Authorization", "Digest " + authStringEnc);
                        } else{
                            String authString = proxyOptions.getUsername() + ":" + proxyOptions.getPassword();
                            String authStringEnc = Base64.getEncoder().encodeToString(authString.getBytes());
                            connection.setRequestProperty("Proxy-Authorization", "Basic " + authStringEnc);
                        }
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
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
            for (HttpHeader header : httpRequest.getHeaders()) {
                for (String value : header.getValues()) {
                    connection.addRequestProperty(header.getName(), value);
                }
            }
            return connection;
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Asynchronously sends the content of an HttpRequest via an HttpUrlConnection instance.
     *
     * @param httpRequest The HTTP Request being sent
     * @param progressReporter A reporter for the progress of the request
     * @param connection The HttpURLConnection that is being sent to
     * @return A Mono that represents the completion of the request sending process
     */
    private Mono<Void> sendBodyAsync(HttpRequest httpRequest, ProgressReporter progressReporter, HttpURLConnection connection) {
        switch (httpRequest.getHttpMethod()) {
            case POST:
            case PUT:
            case DELETE: {
                connection.setDoOutput(true);

                Flux<BinaryData> requestBody;
                BinaryData binaryDataBody = httpRequest.getBodyAsBinaryData();

                if (binaryDataBody != null) {
                    requestBody = Flux.just(binaryDataBody);
                    return requestBody.flatMap(body -> {
                        byte[] buffer = new byte[8192]; // 8KB is a common default, this can be investigated for better options later.
                        InputStream bodyStream = binaryDataBody.toStream();
                        return Flux.using(
                            () -> new DataOutputStream(new BufferedOutputStream(connection.getOutputStream())),
                            os -> Flux.generate(
                                () -> os,
                                (network, sink) -> {
                                    try {
                                        int read = bodyStream.read(buffer);

                                        // Consuming the request body is complete; signal completion.
                                        if (read == -1) {
                                            sink.complete();
                                        } else {
                                            if (progressReporter != null) {
                                                progressReporter.reportProgress(read);
                                            }

                                            network.write(buffer, 0, read);
                                            network.flush();

                                            sink.next(1); // Dummy value to propagate to trigger timeout reset on data being sent.
                                        }
                                    } catch (IOException ex) {
                                        sink.error(ex); // Error during sending the request; terminate the stream.
                                    }
                                    return network;
                                }
                            ).timeout(writeTimeout),
                            os -> {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        );
                    }).then();
                }
            }
            case GET:
            case HEAD:
            case OPTIONS:
            case TRACE:
            case CONNECT: {
                return Mono.empty();
            }
            default: {
                return FluxUtil.monoError(LOGGER, new IllegalStateException("Unknown HTTP Method:" + httpRequest.getHttpMethod()));
            }
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
    private void sendBodySync(HttpRequest httpRequest, ProgressReporter progressReporter, HttpURLConnection connection) {
        switch (httpRequest.getHttpMethod()) {
            case POST:
            case PUT:
            case DELETE: {
                connection.setDoOutput(true);
                BinaryData binaryBodyData = httpRequest.getBodyAsBinaryData();

                if (binaryBodyData != null) {
                    byte[] bytes = binaryBodyData.toBytes();
                    if (progressReporter != null) {
                        progressReporter.reportProgress(bytes.length);
                    }
                    try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()))) {
                        os.write(bytes);
                        os.flush();
                    } catch (IOException e) {
                        throw LOGGER.logExceptionAsError(new RuntimeException(e));
                    }
                }
            }
            case GET:
            case HEAD:
            case OPTIONS:
            case TRACE:
            case CONNECT: {
                break;
            }
            default: {
                throw LOGGER.logExceptionAsError(new IllegalStateException("Unknown HTTP Method:"
                    + httpRequest.getHttpMethod()));
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

            HttpHeaders responseHeaders = new HttpHeaders();
            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                if (entry.getKey() != null) {
                    List<String> values = new ArrayList<>();
                    entry.getValue().forEach(v -> values.add(0, v));
                    for (String headerValue : values) {
                        responseHeaders.add(HttpHeaderName.fromString(entry.getKey()), headerValue);
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

            return new HttpUrlConnectionResponse(
                httpRequest,
                responseCode,
                responseHeaders,
                BinaryData.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray()))
            );
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        } finally {
            connection.disconnect();
        }
    }
}
