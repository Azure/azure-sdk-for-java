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
        return handleConnection(httpRequest, progressReporter);
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

    /**
     * Open a connection based on the HttpRequest URL
     *
     * @param httpRequest The HTTP Request being sent
     * @param progressReporter (Optional) for reporting progress while writing the request body
     * @return A Mono containing a HttpUrlConnectionResponse object
     */
    private Mono<HttpResponse> handleConnection(HttpRequest httpRequest, ProgressReporter progressReporter) {
        return Mono.defer(() -> {
            try {
                // Make connection
                URL url = httpRequest.getUrl();
                HttpURLConnection connection = null;

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

                if (connectionTimeout != null) {
                    connection.setConnectTimeout((int) connectionTimeout.toMillis());
                }

                // SetConnectionRequest
                try {
                    connection.setRequestMethod(httpRequest.getHttpMethod().toString());
                } catch (ProtocolException e) {
                    return FluxUtil.monoError(LOGGER, new RuntimeException(e));
                }
                for (HttpHeader header : httpRequest.getHeaders()) {
                    for (String value : header.getValues()) {
                        connection.addRequestProperty(header.getName(), value);
                    }
                }

                // Write body
                switch (httpRequest.getHttpMethod()) {
                    case POST:
                    case PUT:
                    case DELETE:
                        connection.setDoOutput(true);
                        if (httpRequest.getBody() != null) {
                            try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()))) {

                                Flux<ByteBuffer> requestBody = httpRequest.getBody();
                                if (progressReporter != null) {
                                    requestBody = requestBody.map(buffer -> {
                                        progressReporter.reportProgress(buffer.remaining());
                                        return buffer;
                                    });
                                }

                                requestBody
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
                                    .then()
                                    .block(); // Wait for completion of the write operations
                                os.flush();
                            } catch (IOException e) {
                                return FluxUtil.monoError(LOGGER, new RuntimeException(e));
                            }
                        }
                        break;
                    case GET:
                    case HEAD:
                    case OPTIONS:
                    case TRACE:
                    case CONNECT:
                        break;
                    default:
                        return FluxUtil.monoError(LOGGER, new IllegalStateException("Unknown HTTP Method:"
                            + httpRequest.getHttpMethod()));
                }

                // Read response
                int responseCode = connection.getResponseCode();

                /*Map<String, List<String>> responseHeadersMap = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                    if (entry.getKey() != null) {
                        responseHeadersMap.put(entry.getKey(), entry.getValue());
                    }
                }*/

                HttpHeaders responseHeaders = new HttpHeaders();
                for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                    if (entry.getKey() != null) {
                        for (String headerValue : entry.getValue()) {
                            responseHeaders.add(entry.getKey(), headerValue);
                        }
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

                connection.disconnect();

                return Mono.just(new HttpUrlConnectionResponse(
                    httpRequest,
                    responseCode,
                    responseHeaders,
                    Flux.just(ByteBuffer.wrap(outputStream.toByteArray()))
                ));

            } catch (IOException e) {
                return FluxUtil.monoError(LOGGER, new RuntimeException("Error opening HTTP connection", e));
            }
        });
    }
}
