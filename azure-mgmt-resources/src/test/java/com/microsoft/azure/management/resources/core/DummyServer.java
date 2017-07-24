package com.microsoft.azure.management.resources.core;

import org.eclipse.jetty.server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class DummyServer {
    private final int port;
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
        Server server = new Server(port);
        return new DummyServer(port, server);
    }

    public int getPort() {
        return port;
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        try {
            httpServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean b = thread.isAlive();
        System.out.printf("HTTP server on port %s stopped, thread joined %s\n", port, b);
    }

    private static int findAvailablePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }
}
