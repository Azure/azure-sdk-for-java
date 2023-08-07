package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;

import java.io.*;
import java.net.*;

// A socket client to be used to handle PATCH requests

public class SocketClient {

    private final String host;
    private final int port;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // If no port is specified, set the port to 80
    public SocketClient(String host) {
        this(host, 80);
    }

    // todo - this should return a HttpResponse
    //  need a response handler to do this
    public void sendPatchRequest(HttpRequest httpRequest) throws IOException {
        String request = buildPatchRequest(httpRequest);
        try (Socket socket = new Socket(host, port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            OutputStream out = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            outputStreamWriter.write(request);
            outputStreamWriter.flush();

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
            }

            HttpResponse response = getResponse(httpRequest, sb.toString());

            out.close();
            in.close();
            System.out.println(response);
        }
    }

    // This method extracts an HttpResponse object from the response string
    // todo - this is clunky. It may be better to pass an inputstream
    //  to this method and have it read line by line
    private HttpResponse getResponse(HttpRequest request, String responseString) {
        // Separate the status line and headers from the body
        String headerField = responseString.split("\r\n\r\n")[0];
        byte[] body = responseString.split("\r\n\r\n")[1].getBytes();

        String statusLine = headerField.split("\r\n")[0];
        // Retrieve the status code from the status line
        int dotIndex = statusLine.indexOf('.');
        int statusCode = Integer.parseInt(statusLine.substring(dotIndex+3, dotIndex+5));

        String[] headers = headerField.split("\r\n");

        HttpHeaders httpHeaders = new HttpHeaders();

        for (int i = 1; i < headers.length; i++) {
            String k = headers[i].split(": ")[0];
            String v = headers[i].split(": ")[1];
            // Convert the String key to a HttpHeaderName
            // Add the key and value to the headers
            // todo - may need to check that the HttpHeaderName is valid
            httpHeaders.set(HttpHeaderName.fromString(k), v);
        }
        return new HttpResponse(request, httpHeaders, statusCode, body);
    }

    // todo - make this a generic request builder or leave as strictly PATCH?
    private String buildPatchRequest(HttpRequest httpRequest) {
        final StringBuilder request = new StringBuilder();
        // Add the status line
        request.append(httpRequest.getHttpMethod())
            .append(" ")
            .append(httpRequest.getUrl().getPath())
            .append(" HTTP/1.0");
        System.out.println(request);
        // Add the headers
        // First check if there are any headers to add
        if (httpRequest.getHeaders().getSize() > 0) {
            request.append("\r\n");
            for (HttpHeader headerLine : httpRequest.getHeaders()) {
                request.append(headerLine.getName())
                    .append(": ")
                    .append(headerLine.getValue())
                    .append("\r\n");
            }
        }
        // Add the body
        // Check if there is a body to add
        // todo - add content length check
        if (httpRequest.getBody() != null) {
            request.append("\r\n");
            request.append(httpRequest.getBody().toString());
        }
        // Add end of file
        // todo - is this only needed when there is no content-length header?
        request.append("\r\n\r\n");
        System.out.println(request);
        return request.toString();
    }
}
