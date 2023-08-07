package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;

import java.io.*;
import java.net.*;
import java.util.Arrays;

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

    public HttpResponse sendPatchRequest(HttpRequest httpRequest) throws IOException {
        String request = buildPatchRequest(httpRequest);
        try (Socket socket = new Socket(host, port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            OutputStream out = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            outputStreamWriter.write(request);
            outputStreamWriter.flush();

            HttpResponse response = buildResponse(httpRequest, in);

            out.close();
            in.close();
            return response;
        }
    }

    // This method reads the response line by line and constructs an HttpResponse object
    private HttpResponse buildResponse(HttpRequest request, BufferedReader reader) throws IOException {
        // Read the first line as the status line
        String statusLine = reader.readLine();
        // Extract the status code from the status line
        int dotIndex = statusLine.indexOf('.');
        int statusCode = Integer.parseInt(statusLine.substring(dotIndex+3, dotIndex+6));
        // Read the headers until reaching a newline
        // Extract the key and value, add to headers
        HttpHeaders headers = new HttpHeaders();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String k = line.split(": ")[0];
            String v = line.split(": ")[1];
            // todo - may need to check that the HttpHeaderName is valid
            headers.set(HttpHeaderName.fromString(k), v);
        }
        // Read the newline through
        reader.readLine();
        // The remainder of the response is the body
        StringBuilder bodyString = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            bodyString.append(line);
        }
        // Convert the body String to a byte array needed for the HttpResponse
        byte[] body = bodyString.toString().getBytes();

        return new HttpResponse(request, headers, statusCode, body);
    }

    private String buildPatchRequest(HttpRequest httpRequest) {
        final StringBuilder request = new StringBuilder();
        // Add the status line
        // todo - hard code the method as PATCH
        request.append(httpRequest.getHttpMethod())
            .append(" ")
            .append(httpRequest.getUrl().getPath())
            .append(" HTTP/1.0")
            .append("\r\n");
        // Add the headers
        // First check if there are any headers to add
        if (httpRequest.getHeaders().getSize() > 0) {
            for (HttpHeader headerLine : httpRequest.getHeaders()) {
                request.append(headerLine.getName())
                    .append(": ")
                    .append(headerLine.getValue())
                    .append("\r\n");
            }
        }
        // Add the body if there is a body to add
        if (httpRequest.getBody() != null) {
            request.append("\r\n")
                .append(httpRequest.getBody().toString())
                .append("\r\n");
        }
        // Add carriage return linefeed to mark end of message
        request.append("\r\n");
        return request.toString();
    }
}
