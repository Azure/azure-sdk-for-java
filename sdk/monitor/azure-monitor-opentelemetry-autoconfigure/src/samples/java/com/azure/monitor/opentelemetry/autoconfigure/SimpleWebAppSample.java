// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * A simple long-running web app sample that generates requests, dependencies, traces, and
 * exceptions — ideal for observing customer-facing SDKStats metrics in Azure Monitor.
 *
 * <h3>Test Modes (set via {@code TEST_MODE} environment variable):</h3>
 * <ul>
 *   <li><b>success</b> (default) — normal operation, telemetry reaches ingestion
 *       → {@code Item_Success_Count} visible in Azure Monitor</li>
 *   <li><b>drop</b> — a local mock server returns 400 for all requests
 *       → {@code Item_Dropped_Count} with drop code 400 (visible in mock server console output)</li>
 *   <li><b>retry</b> — a local mock server returns 500 for all requests
 *       → {@code Item_Retry_Count} with retry code 500 (visible in mock server console output)</li>
 * </ul>
 *
 * <p>In drop/retry modes, a mock ingestion server runs on port 9090. Both application telemetry
 * and SDKStats metrics go through the same pipeline, so SDKStats metrics also hit the mock.
 * The mock server logs all received payloads (gunzipped) so you can inspect the generated
 * {@code Item_Dropped_Count} and {@code Item_Retry_Count} TelemetryItems in the console.</p>
 *
 * <p>Set {@code APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL} to a lower value (e.g. 60)
 * so you don't have to wait 15 minutes for the first SDKStats export.</p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>{@code GET /}           — returns "Hello!"</li>
 *   <li>{@code GET /dependency} — makes an outbound HTTP call</li>
 *   <li>{@code GET /error}      — simulates an error span</li>
 *   <li>{@code GET /exception}  — throws and records an exception</li>
 *   <li>{@code GET /load}       — fires a batch of 20 mixed requests internally (for quick volume)</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>
 * # Test Item_Success_Count (default — goes to real Azure Monitor):
 * set APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL=60
 * mvn compile test-compile exec:java -Dexec.mainClass=...SimpleWebAppSample -Dexec.classpathScope=test
 *
 * # Test Item_Dropped_Count (mock server returns 400):
 * set TEST_MODE=drop
 * set APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL=60
 * mvn compile test-compile exec:java -Dexec.mainClass=...SimpleWebAppSample -Dexec.classpathScope=test
 *
 * # Test Item_Retry_Count (mock server returns 500):
 * set TEST_MODE=retry
 * set APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL=60
 * mvn compile test-compile exec:java -Dexec.mainClass=...SimpleWebAppSample -Dexec.classpathScope=test
 * </pre>
 */
public class SimpleWebAppSample {

    // ── Connection string for success mode (real Azure Monitor) ─────────────
    // Read from APPLICATIONINSIGHTS_CONNECTION_STRING env var; if not set, use a placeholder.
    private static final String CONNECTION_STRING_SUCCESS
        = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING") != null
            ? System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING")
            : "InstrumentationKey=<your-instrumentation-key>"
                + ";IngestionEndpoint=https://<region>.in.applicationinsights.azure.com/"
                + ";LiveEndpoint=https://<region>.livediagnostics.monitor.azure.com/"
                + ";ApplicationId=<your-application-id>";

    // ── Connection string for mock server (drop/retry modes) ────────────────
    private static final int MOCK_PORT = 9090;
    private static final String CONNECTION_STRING_MOCK
        = "InstrumentationKey=00000000-0000-0000-0000-000000000000"
            + ";IngestionEndpoint=http://localhost:" + MOCK_PORT + "/"
            + ";LiveEndpoint=http://localhost:" + MOCK_PORT + "/";

    private static final int APP_PORT = 8080;

    private static Tracer tracer;
    private static LongCounter requestCounter;

    public static void main(String[] args) throws IOException {
        // ── 0. Determine test mode ──────────────────────────────────────────
        String testMode = System.getenv("TEST_MODE");
        if (testMode == null || testMode.isEmpty()) {
            testMode = "success";
        }
        testMode = testMode.toLowerCase();

        String connectionString;
        String modeDescription;
        int mockStatusCode = 0;
        switch (testMode) {
            case "drop":
                connectionString = CONNECTION_STRING_MOCK;
                modeDescription = "DROP — mock server returns 400 → Item_Dropped_Count";
                mockStatusCode = 400;
                break;
            case "retry":
                connectionString = CONNECTION_STRING_MOCK;
                modeDescription = "RETRY — mock server returns 500 → Item_Retry_Count";
                mockStatusCode = 500;
                break;
            default:
                connectionString = CONNECTION_STRING_SUCCESS;
                modeDescription = "SUCCESS — real Azure Monitor → Item_Success_Count";
                break;
        }

        // ── 1. Start mock ingestion server for drop/retry modes ─────────────
        if (mockStatusCode > 0) {
            startMockIngestionServer(mockStatusCode);
        }

        // ── 2. Configure OpenTelemetry + Azure Monitor ──────────────────────
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        AzureMonitorAutoConfigure.customize(sdkBuilder, connectionString);
        OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();

        tracer = openTelemetry.getTracer("SimpleWebApp");
        Meter meter = openTelemetry.meterBuilder("SimpleWebApp").build();
        requestCounter = meter.counterBuilder("http.server.request_count").build();

        // ── 3. Start the web app HTTP server ────────────────────────────────
        HttpServer server = HttpServer.create(new InetSocketAddress(APP_PORT), 0);
        server.createContext("/", SimpleWebAppSample::handleRoot);
        server.createContext("/dependency", SimpleWebAppSample::handleDependency);
        server.createContext("/error", SimpleWebAppSample::handleError);
        server.createContext("/exception", SimpleWebAppSample::handleException);
        server.createContext("/load", SimpleWebAppSample::handleLoad);
        server.start();

        System.out.println("========================================================");
        System.out.println(" SimpleWebAppSample running on http://localhost:" + APP_PORT);
        System.out.println(" Mode: " + modeDescription);
        System.out.println("========================================================");
        System.out.println(" Endpoints:");
        System.out.println("   GET /            — hello");
        System.out.println("   GET /dependency   — outbound HTTP call");
        System.out.println("   GET /error        — simulated error span");
        System.out.println("   GET /exception    — throws & records exception");
        System.out.println("   GET /load         — fires 20 mixed requests");
        System.out.println();
        System.out.println(" Test modes (set TEST_MODE env var):");
        System.out.println("   success (default) — Item_Success_Count (real Azure Monitor)");
        System.out.println("   drop              — Item_Dropped_Count (mock → 400)");
        System.out.println("   retry             — Item_Retry_Count  (mock → 500)");
        System.out.println();
        System.out.println(" Tip: set APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL=60");
        System.out.println("      to see customer SDKStats sooner.");
        System.out.println("========================================================");
    }

    // ── Mock Ingestion Server ───────────────────────────────────────────────

    /**
     * Starts a local HTTP server on {@link #MOCK_PORT} that simulates ingestion failures.
     * It handles POST /v2.1/track by returning the given status code and logging the
     * gunzipped request payload so you can inspect SDKStats TelemetryItems in the console.
     */
    @SuppressWarnings("try")
    private static void startMockIngestionServer(int statusCode) throws IOException {
        AtomicInteger requestCount = new AtomicInteger();
        HttpServer mock = HttpServer.create(new InetSocketAddress(MOCK_PORT), 0);
        mock.createContext("/", exchange -> {
            int reqNum = requestCount.incrementAndGet();
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            // Read and gunzip the request body
            String body = "(empty)";
            try {
                byte[] rawBytes = readAllBytes(exchange.getRequestBody());
                if (rawBytes.length > 0) {
                    String contentEncoding = exchange.getRequestHeaders().getFirst("Content-Encoding");
                    if ("gzip".equalsIgnoreCase(contentEncoding)) {
                        body = gunzip(rawBytes);
                    } else {
                        body = new String(rawBytes, StandardCharsets.UTF_8);
                    }
                }
            } catch (Exception e) {
                body = "(failed to read body: " + e.getMessage() + ")";
            }

            // Log the request to console
            boolean isSdkStats = body.contains("Item_Success_Count")
                || body.contains("Item_Dropped_Count")
                || body.contains("Item_Retry_Count");
            String tag = isSdkStats ? " [SDKStats]" : "";
            System.out.println();
            System.out.println("╔══ MOCK INGESTION #" + reqNum + tag + " ══════════════════════════════");
            System.out.println("║ " + method + " " + path + " → returning " + statusCode);
            // Print each telemetry item on its own line for readability
            for (String line : body.split("\n")) {
                System.out.println("║ " + line.trim());
            }
            System.out.println("╚═══════════════════════════════════════════════════════");

            // Return the configured error status with a minimal JSON body
            String responseBody = "{\"itemsReceived\":0,\"itemsAccepted\":0,\"errors\":[]}";
            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });
        mock.start();
        System.out.println("[Mock] Ingestion server started on http://localhost:" + MOCK_PORT
            + " — returning " + statusCode + " for all requests");
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tmp = new byte[4096];
        int n;
        while ((n = is.read(tmp)) != -1) {
            buffer.write(tmp, 0, n);
        }
        return buffer.toByteArray();
    }

    private static String gunzip(byte[] compressed) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new java.io.ByteArrayInputStream(compressed))) {
            return new String(readAllBytes(gis), StandardCharsets.UTF_8);
        }
    }

    // ── Handlers ────────────────────────────────────────────────────────────

    @SuppressWarnings("try")
    private static void handleRoot(HttpExchange exchange) throws IOException {
        Span span = tracer.spanBuilder("GET /").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            requestCounter.add(1, Attributes.of(AttributeKey.stringKey("path"), "/"));
            respond(exchange, 200, "Hello from SimpleWebAppSample!");
        } finally {
            span.end();
        }
    }

    @SuppressWarnings("try")
    private static void handleDependency(HttpExchange exchange) throws IOException {
        Span span = tracer.spanBuilder("GET /dependency").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            requestCounter.add(1, Attributes.of(AttributeKey.stringKey("path"), "/dependency"));

            // Simulate an outbound dependency call
            Span depSpan = tracer.spanBuilder("HTTP GET httpbin.org").setSpanKind(SpanKind.CLIENT).startSpan();
            try (Scope depScope = depSpan.makeCurrent()) {
                URL url = new URL("https://httpbin.org/get");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int status = conn.getResponseCode();
                depSpan.setAttribute("http.status_code", status);
                conn.disconnect();
                respond(exchange, 200, "Dependency call returned " + status);
            } catch (Exception e) {
                depSpan.recordException(e);
                depSpan.setStatus(StatusCode.ERROR, e.getMessage());
                respond(exchange, 502, "Dependency call failed: " + e.getMessage());
            } finally {
                depSpan.end();
            }
        } finally {
            span.end();
        }
    }

    @SuppressWarnings("try")
    private static void handleError(HttpExchange exchange) throws IOException {
        Span span = tracer.spanBuilder("GET /error").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            requestCounter.add(1, Attributes.of(AttributeKey.stringKey("path"), "/error"));
            span.setStatus(StatusCode.ERROR, "simulated server error");
            respond(exchange, 500, "Simulated 500 error");
        } finally {
            span.end();
        }
    }

    @SuppressWarnings("try")
    private static void handleException(HttpExchange exchange) throws IOException {
        Span span = tracer.spanBuilder("GET /exception").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            requestCounter.add(1, Attributes.of(AttributeKey.stringKey("path"), "/exception"));
            try {
                throw new RuntimeException("Something went wrong!");
            } catch (RuntimeException e) {
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, e.getMessage());
                respond(exchange, 500, "Exception recorded: " + e.getMessage());
            }
        } finally {
            span.end();
        }
    }

    @SuppressWarnings("try")
    private static void handleLoad(HttpExchange exchange) throws IOException {
        Span span = tracer.spanBuilder("GET /load").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            // Generate a batch of child spans to create volume
            int count = 20;
            for (int i = 0; i < count; i++) {
                String op = randomOp();
                Span child = tracer.spanBuilder("load-" + op).setSpanKind(SpanKind.INTERNAL).startSpan();
                try (Scope childScope = child.makeCurrent()) {
                    // simulate some work
                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 50));
                    if ("error".equals(op)) {
                        child.setStatus(StatusCode.ERROR, "load error");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    child.end();
                }
            }
            requestCounter.add(count, Attributes.of(AttributeKey.stringKey("path"), "/load"));
            respond(exchange, 200, "Generated " + count + " spans");
        } finally {
            span.end();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String randomOp() {
        String[] ops = {"ok", "ok", "ok", "error", "dep"};
        return ops[ThreadLocalRandom.current().nextInt(ops.length)];
    }
}
