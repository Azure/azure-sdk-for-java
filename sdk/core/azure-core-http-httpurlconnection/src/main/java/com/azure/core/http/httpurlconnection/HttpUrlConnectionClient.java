package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HttpUrlConnectionClient {
    private HttpRequest request;
    private HttpURLConnection connection;

    public HttpUrlConnectionClient(HttpRequest request) {
        this.setRequest(request);
    }

    public HttpUrlConnectionClient() {}

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse send() {
        return this.send(this.request);
    }

    public HttpResponse send(HttpRequest httpRequest) {
        HttpMethod httpMethod = httpRequest.getHttpMethod();
        // For PATCH requests, use the Socket client
        if(httpMethod == HttpMethod.PATCH) {
            return null;
        }

        // Create a URL object for use with HttpUrlConnection
        URL url;
        try {
            url = new URL(httpRequest.getUrl().toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // Setup an HTTP Connection based on the current URL protocol
        try {
            this.connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set the request method (this will be anything except for PATCH)
        try {
            this.connection.setRequestMethod(httpRequest.getHttpMethod().toString());
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        // TODO: Handle headers

        // If it's not a GET request, set Output mode to True
        if(httpMethod != HttpMethod.GET)
            this.connection.setDoOutput(true);

        // For POST queries, we need to send the body of the request via an output stream
        if(httpMethod == HttpMethod.POST)
            writeStream();

        // Check the HTTP Response Code
        // This also sends the query
        int responseCode = getResponseCode();

//        if (responseCode == HttpURLConnection.HTTP_OK)
        byte[] responseBody = readStream().toByteArray();


        System.out.println(new String(responseBody, StandardCharsets.UTF_8));

        return new HttpResponse(this.request, this.request.getHeaders(), responseCode, responseBody);
    }

    private ByteArrayOutputStream readStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream is = null;

        try {
            is = this.connection.getInputStream();
            byte[] chunk = new byte[4096];
            int bytesRead;

            while((bytesRead = is.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return outputStream;
    }

    private void writeStream() {
        try {
            System.out.println(this.request.getBody());
            OutputStream os = this.connection.getOutputStream();
            OutputStreamWriter out = new OutputStreamWriter(os);
            out.write(this.request.getBody().toString());
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
}
