package com.azure.cosmos.avadtest.health;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight HTTP health server for Kubernetes probes.
 *
 * Endpoints:
 *   GET /health  — liveness probe (always 200 if JVM is up)
 *   GET /ready   — readiness probe (200 when workload is ready)
 */
public final class HealthServer {

    private static final Logger log = LoggerFactory.getLogger(HealthServer.class);
    private static final int DEFAULT_PORT = 8080;

    private final HttpServer server;
    private final ExecutorService executor;
    private final AtomicBoolean ready = new AtomicBoolean(false);

    public HealthServer() throws IOException {
        this(DEFAULT_PORT);
    }

    public HealthServer(int port) throws IOException {
        this.executor = Executors.newFixedThreadPool(2);
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.setExecutor(executor);

        server.createContext("/health", exchange -> {
            byte[] body = "{\"status\":\"UP\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.createContext("/ready", exchange -> {
            boolean isReady = ready.get();
            String json = "{\"ready\":" + isReady + "}";
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(isReady ? 200 : 503, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
    }

    public void start() {
        server.start();
        log.info("Health server started on port {}", server.getAddress().getPort());
    }

    public void setReady(boolean isReady) {
        ready.set(isReady);
        log.info("Readiness set to: {}", isReady);
    }

    public void stop() {
        server.stop(2);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        log.info("Health server stopped");
    }
}
