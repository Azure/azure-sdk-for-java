package com.azure.core.http.httpurlconnection;

import com.azure.core.http.*;
import com.azure.core.http.httpurlconnection.implementation.HttpUrlConnectionResponse;
import com.azure.core.util.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.azure.core.http.HttpHeader;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * This class provides a HttpUrlConnection implementation for the {@link HttpClient} interface.
 * Creating an instance of this class can be achieved by using the {@link HttpUrlConnectionAsyncClientBuilder} class.
 *
 * @see HttpClient
 * @see HttpUrlConnectionAsyncClientBuilder
 */
public class HttpUrlConnectionAsyncClient implements HttpClient {
    private final Duration connectionTimeout;
    private final ProxyOptions proxyOptions;
    private final Executor executor;
    private final Configuration configuration;

    public HttpUrlConnectionAsyncClient(Duration connectionTimeout, ProxyOptions proxyOptions, Executor executor, Configuration configuration) {
        this.connectionTimeout = connectionTimeout;
        this.proxyOptions = proxyOptions;
        this.executor = executor;
        this.configuration = configuration;
    }

    // Asynchronous send method returning a Mono of HttpResponse
    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest) {
        return sendAsync(httpRequest, Context.NONE);
    }

    // Asynchronous send method returning a Mono of HttpResponse
    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return sendAsync(request, context);
    }

    // Synchronous send method with additional context, primarily for interface compliance
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
        return openConnection(httpRequest)
            .flatMap(connection -> setConnectionRequest(connection, httpRequest)
                .then(writeRequestBody(connection, httpRequest, progressReporter))
                .then(readResponse(connection, httpRequest))
                .doFinally(signalType -> connection.disconnect()) // Disconnect connection after processing
                .onErrorResume(Mono::error));

    }

    // Send a PATCH request via a SocketClient
    private Mono<HttpResponse> sendPatchViaSocket(HttpRequest httpRequest) {
        return Mono.fromCallable(() -> SocketClient.sendPatchRequest(httpRequest));
    }

    // Open a connection based on the HttpRequest URL
    private Mono<HttpURLConnection> openConnection(HttpRequest httpRequest) {
        return Mono.fromCallable(() -> {
            try {
                URL url = httpRequest.getUrl();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

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
                    if (connectionTimeout != null) {
                        connection.setConnectTimeout((int) connectionTimeout.toMillis());
                    }
                }
                return connection;
            } catch (IOException e) {
                throw new RuntimeException("Error opening HTTP connection", e);
            }
        });
    }

    // Set properties and headers on the HttpURLConnection
    private Mono<Void> setConnectionRequest(HttpURLConnection connection, HttpRequest httpRequest) {
        return Mono.fromRunnable(() -> {
            try {
                connection.setRequestMethod(httpRequest.getHttpMethod().toString());
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            }
            for (HttpHeader header : httpRequest.getHeaders()) {
                for (String value : header.getValues()) {
                    connection.addRequestProperty(header.getName(), value);
                }
            }
            if (httpRequest.getHttpMethod() != HttpMethod.GET) {
                connection.setDoOutput(true);
            }
        });
    }

    // Write the body of the request if necessary
    private Mono<Void> writeRequestBody(HttpURLConnection connection, HttpRequest httpRequest, ProgressReporter progressReporter) {
        switch(httpRequest.getHttpMethod()) {
            case POST:
            case PUT:
            case PATCH:
            case DELETE:
                if (httpRequest.getBody() != null) {
                    return Mono.fromRunnable(() -> {
                        try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()))) {

                            Flux<ByteBuffer> requestBody = httpRequest.getBody();
                            if (progressReporter != null) {
                                requestBody = requestBody.map(buffer -> {
                                    progressReporter.reportProgress(buffer.remaining());
                                    return buffer;
                                });
                            }

                            requestBody
                                .doOnNext(buffer -> {
                                    try {
                                        byte[] bytes = new byte[buffer.remaining()];
                                        buffer.get(bytes);
                                        os.write(bytes);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }).blockLast();
                            os.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    return Mono.empty();
                }
            default:
                return Mono.empty();
        }
    }

    // Read the response and construct the HttpResponse object
    private static Mono<HttpUrlConnectionResponse> readResponse(HttpURLConnection connection, HttpRequest httpRequest) {
        return Mono.fromCallable(() -> {
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
        }).onErrorResume(e -> Mono.error(new RuntimeException("Error reading HTTP response", e)));
    }
}
