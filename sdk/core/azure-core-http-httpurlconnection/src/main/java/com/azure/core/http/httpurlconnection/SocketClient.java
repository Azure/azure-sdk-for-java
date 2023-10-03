package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.httpurlconnection.implementation.HttpUrlConnectionResponse;
import reactor.core.publisher.Flux;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A socket client used for making PATCH requests
 */

class SocketClient {

    private static final String HTTP_VERSION = " HTTP/1.0";

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
        httpRequest.setHeader(HttpHeaderName.HOST, httpRequest.getUrl().getHost());
        String request = buildPatchRequest(httpRequest);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream())) {

            out.write(request);
            out.flush();

            HttpUrlConnectionResponse response = buildResponse(httpRequest, in);

            String redirectLocation = response.getHeaders().stream()
                .filter(h -> h.getName().equals("Location"))
                .map(HttpHeader::getValue)
                .findFirst()
                .orElse(null);

            if (redirectLocation != null) {
                if (!redirectLocation.startsWith("http")) {
                    httpRequest.setUrl(new URL(httpRequest.getUrl(), redirectLocation));
                } else {
                    httpRequest.setUrl(redirectLocation);
                }
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

        request.append("PATCH")
               .append(" ")
               .append(httpRequest.getUrl().getPath())
               .append(HTTP_VERSION)
               .append("\r\n");

        if (httpRequest.getHeaders().getSize() > 0) {
            for (HttpHeader header : httpRequest.getHeaders()) {
                header.getValuesList().forEach(value -> request.append(header.getName())
                                                               .append(": ")
                                                               .append(value)
                                                               .append("\r\n"));
            }
        }
        // Add the body if there is a body to add
        if (httpRequest.getBody() != null) {
            request.append("\r\n")
                   .append(httpRequest.getBodyAsBinaryData().toString())
                   .append("\r\n");
        }
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
        String statusLine = reader.readLine();
        int dotIndex = statusLine.indexOf('.');
        int statusCode = Integer.parseInt(statusLine.substring(dotIndex+3, dotIndex+6));

        HttpHeaders headers = new HttpHeaders();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] kv = line.split(": ", 2);
            String k = kv[0];
            String v = kv[1];
            if (v == null) {
                headers.add(k, v);
            }
        }

        StringBuilder bodyString = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            bodyString.append(line)
                      .append("\n");
        }

        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(bodyString.toString().getBytes()));

        return new HttpUrlConnectionResponse(httpRequest, statusCode, headers, body);
    }
}
