package com.azure.core.http.httpurlconnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// A simple socket server used purely for development/testing. Not to be included in final product

public class SimpleServer {

    public static void main(String[] args) throws IOException {

        try (ServerSocket server = new ServerSocket(9000)) {
            while (true) {
                Socket client = server.accept();
                System.out.println("A client has connected.\n");

                // Print the input stream
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }

    }
}
