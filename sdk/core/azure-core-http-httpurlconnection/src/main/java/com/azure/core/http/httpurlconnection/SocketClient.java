package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpMethod;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

// A socket client to be used to handle PATCH requests
// Not currently a functional class, please do not use at this stage

public class SocketClient {

    private final String host;
    private final int port;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public SocketClient(String host) {
        this.host = host;
        // todo - need logic for determining port number if not specified
        this.port = 80;
        // this.port = 443;
    }

    // todo - this method should take an HttpRequest.
    public void sendPatchRequest() throws IOException, URISyntaxException {
        final URI uri = new URI("http", null, host, port, "/", null, null);
        try (Socket socket = new Socket(uri.getHost(), uri.getPort())) {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);
            printWriter.println("Test");
        }
    }


    // Testing
    public static void main(String[] args) throws IOException, URISyntaxException {
//        SocketClient client = new SocketClient("localhost", 9000);
//        client.sendPatchRequest();



        // Test block

        String requestBody = "This is the body of the request";
        byte[] bodyBytes = requestBody.getBytes();

        HttpRequest httpRequest  = new HttpRequest(HttpMethod.PATCH, "http://www.localhost.com", null, bodyBytes);




    }

}
