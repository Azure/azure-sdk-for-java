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

    public HttpUrlConnectionClient(HttpRequest request) {
        this.setRequest(request);
    }

    public HttpUrlConnectionClient(){}

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public Mono<HttpResponse> send() {
        return send(this.request);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest) {
        return Mono.fromCallable(() -> sendSynchronous(httpRequest));
    }

    @Override
    public HttpResponse sendSync(HttpRequest httpRequest, Context context) {
        return sendSynchronous(httpRequest);
    }

    public HttpResponse sendSynchronous(HttpRequest httpRequest) {
        HttpMethod httpMethod = httpRequest.getHttpMethod();

        if (httpMethod == HttpMethod.PATCH) {
            try {
                new SocketClient(httpRequest.getUrl().toString()).sendPatchRequest(httpRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        URL url;
        try {
            url = new URL(httpRequest.getUrl().toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            this.connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.connection.setRequestMethod(httpMethod.toString());
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        for (HttpHeader header : httpRequest.getHeaders()) {
            this.connection.setRequestProperty(header.getName(), header.getValue());
        }

        if (httpMethod != HttpMethod.GET)
            this.connection.setDoOutput(true);

        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH)
            writeStream(httpRequest);

        // Return a HttpResponse
        return new HttpUrlConnectionResponse(
            httpRequest,
            httpRequest.getHeaders(),
            getResponseCode(),
            readStream().toByteArray()
        );
    }

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

    private int getResponseCode() {
        try {
            return this.connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return HttpClient.super.send(request, context);
    }

}
