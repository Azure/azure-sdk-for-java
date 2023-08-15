package com.azure.core.http.httpurlconnection;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class HttpUrlConnectionTest {
    private static final int PORT = 8000;
    private static HttpServer server;

    private final List<HttpContext> testContexts = new ArrayList<>();
    @BeforeAll
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(null);
        server.start();
    }

    @AfterAll
    public static void stopServer() {
        server.stop(0);
    }
}
