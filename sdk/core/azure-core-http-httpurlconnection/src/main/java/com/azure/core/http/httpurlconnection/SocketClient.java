package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.util.BinaryData;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

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
        this.host = host;
        this.port = 80;
    }

    // todo - this should return a HttpResponse
    //  need a response handler to do this
    public void sendPatchRequest(HttpRequest httpRequest) throws IOException {
        String request = buildPatchRequest(httpRequest);
        try (Socket socket = new Socket(httpRequest.getUrl().getHost(), httpRequest.getUrl().getPort())) {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request.getBytes(StandardCharsets.UTF_8));
        }
    }

    // todo - make this a generic request builder or leave as strictly PATCH?
    private String buildPatchRequest(HttpRequest httpRequest) {
        final StringBuilder request = new StringBuilder();
        // Add the status line
        // todo - hard code this to use PATCH?
        request.append(httpRequest.getHttpMethod())
            .append(" ")
            .append(httpRequest.getUrl().getPath())
            .append(" HTTP/1.1")
            .append("\r\n");
        // Add the headers
        for (HttpHeader headerLine : httpRequest.getHeaders()) {
            request.append(headerLine.getName())
                .append(": ")
                .append(headerLine.getValue())
                .append("\r\n");
        }
        // Add the body
        // todo - add content length check
        request.append("\r\n");
        request.append(httpRequest.getBody().toString());

        return request.toString();
    }


    // Testing/development code
    // Not to be included in final product
    public static void main(String[] args) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.HOST, "localhost");
        headers.set(HttpHeaderName.CONTENT_TYPE, "text/html");

        String test = "This is the body of the request.";
        BinaryData bd = BinaryData.fromBytes(test.getBytes());

        URL url = new URL("http", "localhost", 9000, "/");

        HttpRequest request = new HttpRequest(HttpMethod.PATCH, url, headers, bd);

        SocketClient client = new SocketClient(request.getUrl().getHost(), request.getUrl().getPort());
        client.sendPatchRequest(request);

        // HttpResponse response = client.sendPatchRequest(request);
    }
}
