package com.microsoft.azure.management.resources.core;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.eclipse.jetty.server.Server;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;

import static sun.net.www.protocol.http.AuthCacheValue.Type.Server;

public class DummyServer {
    private final int port;
//    private final HttpServer httpServer;
    private final Thread thread;

    private final Server httpServer;

    private DummyServer(int port, Server server) {
        this.port = port;
        this.httpServer = server;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    httpServer.start();
                    httpServer.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static DummyServer createOnAvailablePort() throws Exception {
        int port = findAvailablePort();
//        HttpServer server = HttpServer.create(new InetSocketAddress(port), port);
//        server.createContext("/", new DummyHandler());
//        server.setExecutor(null); // creates a default executor

        Server server = new Server(port);
        //server.start();
        //server.join();

        return new DummyServer(port, server);
    }

    public int getPort() {
        return port;
    }

    public void start() {
        thread.start();
        //httpServer.start();
    }

    public void stop() {
        try {
            httpServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean b = thread.isAlive();
        System.out.printf("HTTP server on port %s stoped, thread killed %s\n", port, b);
    }

    private static int findAvailablePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

//    private static class DummyHandler implements HttpHandler {
//        @Override
//        public void handle(HttpExchange t) throws IOException {
//
//            try {
//                if (t.getRequestMethod().equalsIgnoreCase("HEAD")) {
//                    t.sendResponseHeaders(200, -1);
//                } else {
//                    String response = "Dummy server response";
//                    t.sendResponseHeaders(200, response.length());
//                    OutputStream os = t.getResponseBody();
//                    os.write(response.getBytes());
//                    os.close();
//                }
//
//            } catch (IOException e) {
//                System.out.println("==> Dummy server handler caught the exception:");
//                e.printStackTrace();
//                //throw e;
//            }
//        }
//    }

}
