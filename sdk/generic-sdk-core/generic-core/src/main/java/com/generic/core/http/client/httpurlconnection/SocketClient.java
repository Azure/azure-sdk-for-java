// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Header;
import com.generic.core.models.Headers;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A socket client used for making PATCH requests
 */

class SocketClient {

    private static final String HTTP_VERSION = " HTTP/1.1";
    private static final SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    /**
     * Opens a socket connection, then writes the PATCH request across the
     * connection and reads the response
     *
     * @param httpRequest The HTTP Request being sent
     * @return an instance of HttpUrlConnectionResponse
     */
    public static HttpUrlConnectionResponse sendPatchRequest(HttpRequest httpRequest) throws IOException {
        final URL requestUrl = httpRequest.getUrl();
        final String protocol = requestUrl.getProtocol();
        final String host = requestUrl.getHost();
        final int port = requestUrl.getPort();

        switch (protocol) {
            case "https": {
                try (SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port)) {
                    return doInputOutput(httpRequest, socket);
                }
            }
            case "http": {
                try (Socket socket = new Socket(host, port)) {
                    return doInputOutput(httpRequest, socket);
                }
            }
        }
        throw new ProtocolException("Only HTTP and HTTPS are supported by this client.");
    }

    /**
     * Calls buildAndSend to send a String representation of the request across the output
     * stream, then calls buildResponse to get an instance of HttpUrlConnectionResponse
     * from the input stream
     *
     * @param httpRequest The HTTP Request being sent
     * @param socket An instance of the SocketClient
     * @return an instance of HttpUrlConnectionResponse
     */
    private static HttpUrlConnectionResponse doInputOutput(HttpRequest httpRequest, Socket socket) throws IOException {
        httpRequest.setHeader(HttpHeaderName.HOST, httpRequest.getUrl().getHost());
        if (!"keep-alive".equalsIgnoreCase(httpRequest.getHeaders().getValue(HttpHeaderName.CONNECTION))) {
            httpRequest.setHeader(HttpHeaderName.CONNECTION, "close");
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream())) {

            buildAndSend(httpRequest, out);
            HttpUrlConnectionResponse response = buildResponse(httpRequest, in);

            String redirectLocation = response.getHeaders().stream()
                .filter(h -> h.getName().equalsIgnoreCase("Location"))
                .map(Header::getValue)
                .findFirst()
                .orElse(null);

            if (redirectLocation != null) {
                if (redirectLocation.startsWith("http")) {
                    httpRequest.setUrl(redirectLocation);
                } else {
                    httpRequest.setUrl(new URL(httpRequest.getUrl(), redirectLocation));
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
     * @param httpRequest The HTTP Request being sent
     * @param out output stream for writing the request
     */
    private static void buildAndSend(HttpRequest httpRequest, OutputStreamWriter out) throws IOException {
        final StringBuilder request = new StringBuilder();

        request.append("PATCH")
            .append(" ")
            .append(httpRequest.getUrl().getPath())
            .append(HTTP_VERSION)
            .append("\r\n");

        if (httpRequest.getHeaders().getSize() > 0) {
            for (Header header : httpRequest.getHeaders()) {
                header.getValuesList().forEach(value -> request.append(header.getName())
                    .append(": ")
                    .append(value)
                    .append("\r\n"));
            }
        }
        if (httpRequest.getBodyAsBinaryData() != null) {
            request.append("\r\n")
                .append(httpRequest.getBodyAsBinaryData().toString())
                .append("\r\n");
        }

        out.write(request.toString());
        out.flush();
    }

    /**
     * Reads the response from the input stream and extracts the information
     * needed to construct an instance of HttpUrlConnectionResponse
     *
     * @param httpRequest The HTTP Request being sent
     * @param reader the input stream from the socket
     * @return an instance of HttpUrlConnectionResponse
     */
    private static HttpUrlConnectionResponse buildResponse(HttpRequest httpRequest, BufferedReader reader) throws IOException {
        String statusLine = reader.readLine();
        int dotIndex = statusLine.indexOf('.');
        int statusCode = Integer.parseInt(statusLine.substring(dotIndex+3, dotIndex+6));

        Headers headers = new Headers();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] kv = line.split(": ", 2);
            String k = kv[0];
            String v = kv[1];
            headers.add(HttpHeaderName.fromString(k), v);
        }

        StringBuilder bodyString = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            bodyString.append(line);
        }

        BinaryData body = BinaryData.fromByteBuffer(ByteBuffer.wrap(bodyString.toString().getBytes()));

        return new HttpUrlConnectionResponse(httpRequest, statusCode, headers, body);
    }
}
