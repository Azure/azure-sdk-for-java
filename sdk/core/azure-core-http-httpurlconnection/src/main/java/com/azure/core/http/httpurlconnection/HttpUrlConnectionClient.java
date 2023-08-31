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

    

        // Check if the HTTP method is PATCH, if so, use the SocketClient to handle it
        if (httpMethod == HttpMethod.PATCH) {
            try {
                new SocketClient(httpRequest.getUrl().toString()).sendPatchRequest(httpRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Convert the given URL string into a URL object
        URL url;
        try {
            url = new URL(httpRequest.getUrl().toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // Initialize the HttpURLConnection object
        try {
            this.connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set the request method for the HttpURLConnection
        try {
            this.connection.setRequestMethod(httpMethod.toString());
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        // Set request properties (headers) from the provided HttpRequest
        for (HttpHeader header : httpRequest.getHeaders()) {
            this.connection.setRequestProperty(header.getName(), header.getValue());
        }

        // If the method is other than GET, enable output for the connection
        if (httpMethod != HttpMethod.GET)
            this.connection.setDoOutput(true);

        // For POST, PUT, and PATCH methods, write the request body
        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH)
            writeStream(httpRequest);

        // Construct and return the HttpResponse
        return new HttpUrlConnectionResponse(
            httpRequest,
            httpRequest.getHeaders(),
            getResponseCode(),
            readStream().toByteArray()
        );
    }

    // Utility method to read the response stream
    private ByteArrayOutputStream readStream() {
        // Stream to hold our received bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Declare the inputstream we're about to use
        InputStream is;

        try {
            // Get the connection's input stream
            is = this.connection.getInputStream();
            // Chunk to read bytes to. 4MB is both large and small enough, it seems.
            byte[] chunk = new byte[4096];
            // Count how many bytes we just read
            int bytesRead;

            // Loop through the inputstream while we're still receiving bytes from it
            while((bytesRead = is.read(chunk)) > 0) {
                // Write the bytes to the output stream
                outputStream.write(chunk, 0, bytesRead);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Once we're done with the above try{} block, attempt to close the inputstream
        // As long as it actually exists
        try {
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream;
    }

    // Utility method to write the request body
    private void writeStream(HttpRequest httpRequest) {
        try {
            // Get the output stream of the connection
            OutputStream os = this.connection.getOutputStream();
            // Create writer we can use to send info across the stream
            OutputStreamWriter out = new OutputStreamWriter(os);
            // Write the whole body to the writer
            // Body comes as a Flux<ByteBuffer>, so process it down to the out.write command
            httpRequest
                .getBody()
                .map(s -> StandardCharsets.UTF_8.decode(s).toString())
                .subscribe(i -> {
                    try {
                        out.write(i);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            // And close it
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Utility method to get the response code
    private int getResponseCode() {
        try {
            return this.connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
