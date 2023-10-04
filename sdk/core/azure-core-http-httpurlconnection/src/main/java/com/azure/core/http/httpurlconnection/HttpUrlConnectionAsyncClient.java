package com.azure.core.http.httpurlconnection;

import com.azure.core.http.*;
import com.azure.core.http.httpurlconnection.implementation.HttpUrlConnectionResponse;
import com.azure.core.util.*;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.azure.core.http.HttpHeader;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;

/**
 * This class provides a HttpUrlConnection implementation for the {@link HttpClient} interface.
 * Creating an instance of this class can be achieved by using the {@link HttpUrlConnectionAsyncClientBuilder} class.
 *
 * @see HttpClient
 * @see HttpUrlConnectionAsyncClientBuilder
 */
public class HttpUrlConnectionAsyncClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(HttpUrlConnectionAsyncClient.class);
    private final Duration connectionTimeout;
    private final ProxyOptions proxyOptions;
    private final Configuration configuration;

    HttpUrlConnectionAsyncClient(Duration connectionTimeout, ProxyOptions proxyOptions, Configuration configuration) {
        this.connectionTimeout = connectionTimeout;
        this.proxyOptions = proxyOptions;
        this.configuration = configuration;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest) {
        return sendAsync(httpRequest, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return sendAsync(request, context);
    }

    @Override
    public HttpResponse sendSync(HttpRequest httpRequest, Context context) {
        return sendAsync(httpRequest, context).block();
    }

    /**
     * Asynchronously send the HttpRequest
     *
     * @param httpRequest The HTTP Request being sent
     * @param context The context of the request, for any additional changes
     * @return A Mono containing a HttpResponse object
     */
    private Mono<HttpResponse> sendAsync(HttpRequest httpRequest, Context context) {
        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();
        HttpMethod httpMethod = httpRequest.getHttpMethod();
        if (httpMethod == HttpMethod.PATCH) {
            return sendPatchViaSocket(httpRequest);
        }

        return Mono.fromCallable(() -> {
            HttpURLConnection connection = connect(httpRequest);
            Mono<HttpResponse> response = sendRequest(httpRequest, progressReporter, connection)
                .then(Mono.fromCallable(() -> receiveResponse(httpRequest, connection)));
            return response.block();
        });
    }

    /**
     * Send a PATCH request via a SocketClient
     *
     * @param httpRequest The HTTP Request being sent
     * @return A Mono containing a HttpResponse object
     */
    private Mono<HttpResponse> sendPatchViaSocket(HttpRequest httpRequest) {
        return Mono.fromCallable(() -> SocketClient.sendPatchRequest(httpRequest));
    }

    private HttpURLConnection connect(HttpRequest httpRequest) {
        try {
            // Make connection
            HttpURLConnection connection = null;
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
                }
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            assert connection != null;

            if (connectionTimeout != null) {
                connection.setConnectTimeout((int) connectionTimeout.toMillis());
            }

            // SetConnectionRequest
            try {
                connection.setRequestMethod(httpRequest.getHttpMethod().toString());
            } catch (ProtocolException ignored) {}

            for (HttpHeader header : httpRequest.getHeaders()) {
                for (String value : header.getValues()) {
                    connection.addRequestProperty(header.getName(), value);
                }
            }
            return connection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<Void> sendRequest(HttpRequest httpRequest, ProgressReporter progressReporter, HttpURLConnection connection) {
        Mono<Void> requestSendMono = Mono.empty();

        switch (httpRequest.getHttpMethod()) {
            case POST:
            case PUT:
            case DELETE:
                connection.setDoOutput(true);

                // Body we're going to write to the request
                Flux<ByteBuffer> requestBody;

                // Ensure the body is either valid, or we're sending *something*
                if (httpRequest.getBody() == null) requestBody = Flux.just(ByteBuffer.wrap(new byte[0]));
                else {
                    requestBody = httpRequest.getBody();
                }

                try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()))) {
                    if (progressReporter != null) {
                        requestBody = requestBody.map(buffer -> {
                            progressReporter.reportProgress(buffer.remaining());
                            return buffer;
                        });
                    }

                    requestSendMono = requestBody
                        .flatMap(buffer -> {
                            try {
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                os.write(bytes);
                                return Mono.just(buffer); // Emit the buffer for downstream processing if needed
                            } catch (IOException e) {
                                return FluxUtil.monoError(LOGGER, new RuntimeException(e));
                            }
                        })
                        .then(Mono.fromRunnable(() -> {
                            try {
                                os.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }));
                } catch (IOException e) {
                    break;
                }
            case GET:
            case HEAD:
            case OPTIONS:
            case TRACE:
            case CONNECT:
                break ;
            default:
                requestSendMono = FluxUtil.monoError(LOGGER, new IllegalStateException("Unknown HTTP Method:"
                    + httpRequest.getHttpMethod()));
        }
        return requestSendMono;
//        return requestSendMono.then(Mono.fromCallable(() -> {
//            // Read response
//            try {
//                int responseCode = connection.getResponseCode();
//
//                Map<String, List<String>> responseHeadersMap = new HashMap<>();
//                for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
//                    if (entry.getKey() != null) {
//                        responseHeadersMap.put(entry.getKey(), entry.getValue());
//                    }
//                }
//
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                try (InputStream errorStream = connection.getErrorStream()) {
//                    InputStream inputStream = (errorStream == null) ? connection.getInputStream() : errorStream;
//                    byte[] buffer = new byte[1024];
//                    int length;
//                    while ((length = inputStream.read(buffer)) != -1) {
//                        outputStream.write(buffer, 0, length);
//                    }
//                }
//
//                connection.disconnect();
//
//                return new HttpUrlConnectionResponse(
//                    httpRequest,
//                    responseCode,
//                    responseHeadersMap,
//                    Flux.just(ByteBuffer.wrap(outputStream.toByteArray())));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }));
    }

    private HttpResponse receiveResponse(HttpRequest httpRequest, HttpURLConnection connection) {
        try {
            // Read response
            int responseCode = connection.getResponseCode();

            Map<String, List<String>> responseHeadersMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                if (entry.getKey() != null) {
                    responseHeadersMap.put(entry.getKey(), entry.getValue());
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (InputStream errorStream = connection.getErrorStream()) {
                InputStream inputStream = (errorStream == null) ? connection.getInputStream() : errorStream;
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

            return new HttpUrlConnectionResponse(
                httpRequest,
                responseCode,
                responseHeadersMap,
                Flux.just(ByteBuffer.wrap(outputStream.toByteArray()))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Open a connection based on the HttpRequest URL
     *
     * @param httpRequest The HTTP Request being sent
     * @param progressReporter (Optional) for reporting progress while writing the request body
     * @return A Mono containing a HttpUrlConnectionResponse object
     */
}
