package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpMethod;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HttpUrlConnectionClient {
    private HttpRequest request;
    private HttpURLConnection connection;

    public HttpUrlConnectionClient(HttpRequest request) {
        this.setRequest(request);
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse send() {
        return this.send(this.request);
    }

    public HttpResponse send(HttpRequest httpRequest) {
        // Pull out the HTTP Method, as we check it in multiple places
        HttpMethod httpMethod = httpRequest.getHttpMethod();

        // For PATCH requests, use the Socket client
        if(httpMethod == HttpMethod.PATCH) {
            try {
                /*return*/ new SocketClient(httpRequest.getUrl().toString()).sendPatchRequest(httpRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            this.connection.setRequestMethod(httpMethod.toString());
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        // TODO: Handle headers

        // If it's not a GET request, set Output mode to True
        if(httpMethod != HttpMethod.GET)
            this.connection.setDoOutput(true);

        // TODO: Should the above and below be the same if statement?
        // Need to double check if the only data-carrying request is POST.

        // For POST queries, we need to send the body of the request via an output stream
        if(httpMethod == HttpMethod.POST)
            writeStream();

        // Check the HTTP Response Code
        // This also sends the query
        int responseCode = getResponseCode();

        // Fetch any sent response.
        byte[] responseBody = readStream().toByteArray();

        // Testing debug output TODO: remove
        System.out.println(new String(responseBody, StandardCharsets.UTF_8));

        // Return a HTTP Response with all the information we've received back.
        return new HttpResponse(this.request, this.request.getHeaders(), responseCode, responseBody);
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

    private void writeStream() {
        try {
            // Debug output TODO: remove
            System.out.println(this.request.getBody());
            // Get the output stream of the connection
            OutputStream os = this.connection.getOutputStream();
            // Create writer we can use to send info across the stream
            OutputStreamWriter out = new OutputStreamWriter(os);
            // Write the whole  body to the writer
            out.write(this.request.getBody().toString());
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
}
