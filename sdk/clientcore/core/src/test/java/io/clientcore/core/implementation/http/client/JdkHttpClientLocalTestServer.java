// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.shared.LocalTestServer;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

/**
 * {@link LocalTestServer} used by all tests in this package.
 */
public final class JdkHttpClientLocalTestServer {
    private static volatile LocalTestServer server;
    private static final Semaphore SERVER_SEMAPHORE = new Semaphore(1);

    private static volatile LocalTestServer proxyServer;
    private static final Semaphore PROXY_SERVER_SEMAPHORE = new Semaphore(1);

    public static final String TIMEOUT = "/timeout";

    public static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    public static final byte[] LONG_BODY = createLongBody();

    public static final String PROXY_USERNAME = "foo";
    public static final String PROXY_PASSWORD = "bar";

    public static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";

    private static final String SSE_RESPONSE = "/serversentevent";

    /**
     * Gets the {@link LocalTestServer} instance.
     *
     * @return The {@link LocalTestServer} instance.
     */
    public static LocalTestServer getServer() {
        if (server == null) {
            SERVER_SEMAPHORE.acquireUninterruptibly();
            try {
                if (server == null) {
                    server = initializeServer();
                }
            } finally {
                SERVER_SEMAPHORE.release();
            }
        }

        return server;
    }

    private static LocalTestServer initializeServer() {
        LocalTestServer testServer
            = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (req, resp, requestBody) -> {
                String path = req.getServletPath();
                boolean get = "GET".equalsIgnoreCase(req.getMethod());
                boolean post = "POST".equalsIgnoreCase(req.getMethod());
                boolean put = "PUT".equalsIgnoreCase(req.getMethod());

                if (get && "/default".equals(path)) {
                    resp.setStatus(200);
                    resp.setContentLength(0);
                    resp.setContentType("application/json");
                    resp.flushBuffer();
                } else if (get && "/short".equals(path)) {
                    resp.setStatus(200);
                    resp.setContentType("application/octet-stream");
                    resp.setContentLength(SHORT_BODY.length);
                    resp.getOutputStream().write(SHORT_BODY);
                    resp.flushBuffer();
                } else if (get && "/long".equals(path)) {
                    resp.setStatus(200);
                    resp.setContentType("application/octet-stream");
                    resp.setContentLength(LONG_BODY.length);
                    resp.getOutputStream().write(LONG_BODY);
                    resp.flushBuffer();
                } else if (get && "/error".equals(path)) {
                    resp.setStatus(500);
                    resp.setContentLength(5);
                    resp.setContentType("application/text");
                    resp.getOutputStream().write("error".getBytes(StandardCharsets.UTF_8));
                    resp.flushBuffer();
                } else if (get && RETURN_HEADERS_AS_IS_PATH.equals(path)) {
                    resp.setStatus(200);
                    List<String> headerNames = Collections.list(req.getHeaderNames());
                    headerNames.forEach(headerName -> {
                        List<String> headerValues = Collections.list(req.getHeaders(headerName));
                        headerValues.forEach(headerValue -> resp.addHeader(headerName, headerValue));
                    });
                    resp.setContentLength(0);
                    resp.setContentType("application/json");
                    resp.flushBuffer();
                } else if (get && "/empty".equals(path)) {
                    resp.setStatus(200);
                    resp.setContentType("application/octet-stream");
                    resp.setContentLength(0);
                    resp.flushBuffer();
                } else if (post && "/shortPost".equals(path)) {
                    resp.setStatus(200);
                    resp.setContentType("application/octet-stream");
                    resp.setContentLength(SHORT_BODY.length);
                    resp.getOutputStream().write(SHORT_BODY);
                    resp.flushBuffer();
                } else if (get && "/connectionClose".equals(path)) {
                    resp.getHttpChannel().getConnection().close();
                } else if (post && "/shortPostWithBodyValidation".equals(path)) {
                    if (!ByteBuffer.wrap(LONG_BODY, 1, 42).equals(ByteBuffer.wrap(requestBody, 0, 42))) {
                        resp.sendError(400, "Request body does not match expected value");
                    }
                } else if (get && "/noResponse".equals(path)) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else if (get && "/slowResponse".equals(path)) {
                    resp.setStatus(200);
                    resp.setContentLength(SHORT_BODY.length);
                    resp.setContentType("application/octet-stream");
                    resp.setBufferSize(4);
                    resp.getOutputStream().write(SHORT_BODY, 0, 5);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    resp.getOutputStream().write(SHORT_BODY, 5, 3);
                    resp.flushBuffer();
                } else if (get && TIMEOUT.equals(path)) {
                    try {
                        Thread.sleep(5000);
                        resp.setStatus(200);
                        resp.setContentType("application/octet-stream");
                        resp.getOutputStream().write(SHORT_BODY);
                        resp.flushBuffer();
                    } catch (InterruptedException e) {
                        throw new ServletException(e);
                    }
                } else if (get && SSE_RESPONSE.equals(path)) {
                    if (req.getHeader("Last-Event-Id") != null) {
                        sendSSELastEventIdResponse(resp);
                    } else {
                        sendSSEResponseWithRetry(resp);
                    }
                } else if (post && SSE_RESPONSE.equals(path)) {
                    sendSSEResponseWithDataOnly(resp);
                } else if (put && SSE_RESPONSE.equals(path)) {
                    resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
                    resp.setStatus(200);
                    resp.getOutputStream().write(("msg hello world \n\n").getBytes());
                    resp.flushBuffer();
                } else {
                    throw new ServletException("Unexpected request " + req.getMethod() + " " + path);
                }
            });

        testServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(testServer::stop));

        return testServer;
    }

    private static void sendSSEResponseWithDataOnly(org.eclipse.jetty.server.Response resp) throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(("data: YHOO\ndata: +2\ndata: 10\n\n").getBytes());
        resp.flushBuffer();
    }

    private static String addServerSentEventWithRetry() {
        return ": test stream\ndata: first event\nid: 1\nretry: 100\n\n"
            + "data: This is the second message, it\ndata: has two lines.\nid: 2\n\ndata:  third event";
    }

    private static void sendSSEResponseWithRetry(org.eclipse.jetty.server.Response resp) throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(addServerSentEventWithRetry().getBytes());
        resp.flushBuffer();
    }

    private static String addServerSentEventLast() {
        return "data: This is the second message, it\ndata: has two lines.\nid: 2\n\ndata:  third event";
    }

    private static void sendSSELastEventIdResponse(org.eclipse.jetty.server.Response resp) throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(addServerSentEventLast().getBytes());
        resp.flushBuffer();
    }

    private static byte[] createLongBody() {
        byte[] duplicateBytes = "abcdefghijk".getBytes(StandardCharsets.UTF_8);
        byte[] longBody = new byte[duplicateBytes.length * 100000];

        for (int i = 0; i < 100000; i++) {
            System.arraycopy(duplicateBytes, 0, longBody, i * duplicateBytes.length, duplicateBytes.length);
        }

        return longBody;
    }

    /**
     * Gets the proxy {@link LocalTestServer} instance.
     *
     * @return The proxy {@link LocalTestServer} instance.
     */
    public static LocalTestServer getProxyServer() {
        if (proxyServer == null) {
            PROXY_SERVER_SEMAPHORE.acquireUninterruptibly();
            try {
                if (proxyServer == null) {
                    proxyServer = initializeProxyServer();
                }
            } finally {
                PROXY_SERVER_SEMAPHORE.release();
            }
        }

        return proxyServer;
    }

    private static LocalTestServer initializeProxyServer() {
        LocalTestServer proxyServer
            = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (req, resp, requestBody) -> {
                String requestUrl = req.getRequestURL().toString();
                if (!Objects.equals(requestUrl, "/default")) {
                    throw new ServletException("Unexpected request to proxy server");
                }

                String proxyAuthorization = req.getHeader("Proxy-Authorization");
                if (proxyAuthorization == null) {
                    resp.setStatus(407);
                    resp.setHeader("Proxy-Authenticate", "Basic");
                    return;
                }

                if (!proxyAuthorization.startsWith("Basic")) {
                    resp.setStatus(401);
                    return;
                }

                String encodedCred = proxyAuthorization.substring("Basic".length());
                encodedCred = encodedCred.trim();
                final Base64.Decoder decoder = Base64.getDecoder();
                final byte[] decodedCred = decoder.decode(encodedCred);
                if (!new String(decodedCred).equals(PROXY_USERNAME + ":" + PROXY_PASSWORD)) {
                    resp.setStatus(401);
                }
            });

        proxyServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(proxyServer::stop));

        return proxyServer;
    }

    private JdkHttpClientLocalTestServer() {
    }
}
