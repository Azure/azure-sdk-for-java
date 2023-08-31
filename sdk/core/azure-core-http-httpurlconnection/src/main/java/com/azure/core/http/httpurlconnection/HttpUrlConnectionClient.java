package com.azure.core.http.httpurlconnection;

import com.azure.core.http.*;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HttpUrlConnectionClient implements HttpClient {

    private HttpRequest request;
    private HttpURLConnection connection;

    // Default constructor
    public HttpUrlConnectionClient(){}

    // Constructor initializing with a specific request
    public HttpUrlConnectionClient(HttpRequest request) {
        this.setRequest(request);
    }

    // Setter for HttpRequest
    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    // Asynchronous send method returning a Mono of HttpResponse
    public Mono<HttpResponse> send() {
        return sendAsync(this.request, Context.NONE);
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

    // Asynchronous send method with additional context
    public Mono<HttpResponse> sendAsync(HttpRequest httpRequest, Context context) {
        return openConnection(httpRequest)
            .flatMap(connection -> {
                HttpMethod httpMethod = httpRequest.getHttpMethod();

                if (httpMethod == HttpMethod.PATCH) {
                    return sendPatchViaSocket(httpRequest);
                }

                return setConnectionRequest(connection, httpRequest)
                    .then(writeRequestBody(connection, httpRequest))
                    .then(readResponse(connection, httpRequest))
                    .onErrorResume(e -> Mono.error(new RuntimeException(e)));
            });
    }

    // Send a PATCH request via a SocketClient
    private Mono<HttpResponse> sendPatchViaSocket(HttpRequest httpRequest) {
        return Mono.fromRunnable(() -> {
            try {
                new SocketClient(httpRequest.getUrl().toString()).sendPatchRequest(httpRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).then(Mono.empty());
    }

    // Open a connection based on the HttpRequest URL
    private Mono<HttpURLConnection> openConnection(HttpRequest httpRequest) {
        return Mono.fromCallable(() -> {
            URL url = new URL(httpRequest.getUrl().toString());
            return (HttpURLConnection) url.openConnection();
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
                connection.setRequestProperty(header.getName(), header.getValue());
            }
            if (httpRequest.getHttpMethod() != HttpMethod.GET)
                connection.setDoOutput(true);
        });
    }

    // Write the body of the request if necessary
    private Mono<Void> writeRequestBody(HttpURLConnection connection, HttpRequest httpRequest) {
        if (httpRequest.getHttpMethod() == HttpMethod.POST || httpRequest.getHttpMethod() == HttpMethod.PUT || httpRequest.getHttpMethod() == HttpMethod.PATCH) {
            return Mono.fromRunnable(() -> {
                try (OutputStream os = connection.getOutputStream();
                     OutputStreamWriter out = new OutputStreamWriter(os)) {
                    httpRequest.getBody()
                        .map(buffer -> StandardCharsets.UTF_8.decode(buffer).toString())
                        .doOnNext(bodyString -> {
                            try {
                                out.write(bodyString);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).blockLast();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return Mono.empty();
    }

    // Read the response and construct the HttpResponse object
    private Mono<HttpResponse> readResponse(HttpURLConnection connection, HttpRequest httpRequest) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (InputStream is = connection.getInputStream()) {
                byte[] chunk = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(chunk)) > 0) {
                    outputStream.write(chunk, 0, bytesRead);
                }
                return new HttpUrlConnectionResponse(httpRequest, httpRequest.getHeaders(), connection.getResponseCode(), outputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
