package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.httpurlconnection.implementation.HttpUrlConnectionResponse;
import reactor.core.publisher.Flux;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * A socket client used for making PATCH requests
 */

class SocketClient {

    /**
     * Opens a socket connection, then writes the PATCH request across the
     * connection and reads the response
     *
     * @param httpRequest {@link com.azure.core.http.HttpRequest} instance
     * @return an instance of HttpUrlConnectionResponse
     */
    public static HttpUrlConnectionResponse sendPatchRequest(HttpRequest httpRequest) throws IOException {
        String host = httpRequest.getUrl().getHost();
        int port = httpRequest.getUrl().getPort();
        if (httpRequest.getUrl().getProtocol().equals("https")) {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try (SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port)) {
                return doInputOutput(httpRequest, socket);
            }
        } else if (httpRequest.getUrl().getProtocol().equals("http")) {
            try (Socket socket = new Socket(host, port)) {
                return doInputOutput(httpRequest, socket);
            }
        }
        // A protocol other than HTTP or HTTPS has been specified, throw an exception
        throw new ProtocolException("Only HTTP and HTTPS are supported by this client.");
    }

    /**
     * Gets a String representation of the request and writes it to the output stream,
     * then calls buildResponse to get an instance of HttpUrlConnectionResponse from the
     * input stream
     *
     * @param httpRequest {@link com.azure.core.http.HttpRequest} instance
     * @param socket {@link java.net.Socket} instance
     * @return an instance of HttpUrlConnectionResponse
     */
    private static HttpUrlConnectionResponse doInputOutput(HttpRequest httpRequest, Socket socket) throws IOException {
        String request = buildPatchRequest(httpRequest);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream())) {

            out.write(request);
            out.flush();

            HttpUrlConnectionResponse response = buildResponse(httpRequest, in);

            // If the location header is set in the response, follow the redirect
            String redirectLocation = response.getHeaders().stream()
                .filter(h -> h.getName().equals("Location"))
                .map(HttpHeader::getValue)
                .findFirst()
                .orElse(null);

            if (redirectLocation != null) {
                httpRequest.setUrl(redirectLocation);
                return sendPatchRequest(httpRequest);
            }
            return response;
        }
    }

    /**
     * Converts an instance of HttpRequest to a String representation for sending
     * over the output stream
     *
     * @param httpRequest {@link com.azure.core.http.HttpRequest} instance
     * @return the String representation of the HttpRequest
     */
    private static String buildPatchRequest(HttpRequest httpRequest) {
        final StringBuilder request = new StringBuilder();
        // Add the status line
        request.append("PATCH")
            .append(" ")
            .append(httpRequest.getUrl().getPath())
            .append(" HTTP/1.1")
            .append("\r\n");
        // Add the headers if there are headers to add
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
                .append(httpRequest.getBodyAsBinaryData().toString())
                .append("\r\n");
        }
        // Add carriage return linefeed to mark end of message
        request.append("\r\n");
        return request.toString();
    }

    /**
     * Reads the response from the input stream and extracts the information
     * needed to construct an instance of HttpUrlConnectionResponse
     *
     * @param httpRequest {@link com.azure.core.http.HttpRequest} instance
     * @param reader {@link java.io.BufferedReader} instance
     * @return an instance of HttpUrlConnectionResponse
     */
    private static HttpUrlConnectionResponse buildResponse(HttpRequest httpRequest, BufferedReader reader) throws IOException {
        // Read the first line as the status line
        String statusLine = reader.readLine();
        // Extract the status code from the status line
        int dotIndex = statusLine.indexOf('.');
        int statusCode = Integer.parseInt(statusLine.substring(dotIndex+3, dotIndex+6));
        // Read the headers until reaching a newline
        // Extract each key/value pair, add to headers
        Map<String, List<String>> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String k = line.split(": ")[0];
            String v = line.split(": ")[1];
            headers.computeIfAbsent(k, key -> new ArrayList<>()).add(v);
        }
        // Read the newline through
        reader.readLine();
        // The remainder of the response is the body
        StringBuilder bodyString = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            bodyString.append(line).append("\n"); // Preserve newline characters
        }
        // Convert the body String to a Flux<ByteBuffer> needed for the HttpResponse
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(bodyString.toString().getBytes()));

        return new HttpUrlConnectionResponse(httpRequest, statusCode, headers, body);
    }
}
