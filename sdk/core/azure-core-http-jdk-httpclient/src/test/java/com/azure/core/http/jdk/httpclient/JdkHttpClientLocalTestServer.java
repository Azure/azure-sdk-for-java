// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient;

import com.azure.core.test.shared.LocalTestServer;
import org.eclipse.jetty.util.Callback;

import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
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
        LocalTestServer testServer = new LocalTestServer((req, resp, requestBody) -> {
            String path = req.getServletPath();
            boolean get = "GET".equalsIgnoreCase(req.getMethod());
            boolean post = "POST".equalsIgnoreCase(req.getMethod());

            if (get && "/default".equals(path)) {
                resp.setStatus(200);
                resp.flushBuffer();
            } else if (get && "/short".equals(path)) {
                resp.setContentLength(SHORT_BODY.length);
                resp.getHttpOutput().write(SHORT_BODY);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && "/long".equals(path)) {
                resp.setContentLength(LONG_BODY.length);
                resp.getHttpOutput().write(LONG_BODY);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && "/error".equals(path)) {
                resp.setStatus(500);
                resp.setContentLength(5);
                resp.getHttpOutput().write("error".getBytes(StandardCharsets.UTF_8));
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (post && "/shortPost".equals(path)) {
                resp.setContentLength(SHORT_BODY.length);
                resp.getHttpOutput().write(SHORT_BODY);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && "/connectionClose".equals(path)) {
                resp.getHttpChannel().getConnection().close();
            } else if (post && "/shortPostWithBodyValidation".equals(path)) {
                if (!Arrays.equals(LONG_BODY, 1, 43, requestBody, 0, 42)) {
                    resp.sendError(400, "Request body does not match expected value");
                }
            } else if (get && "/noResponse".equals(path)) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if (get && "/slowResponse".equals(path)) {
                resp.setContentLength(SHORT_BODY.length);
                resp.setBufferSize(4);
                resp.getHttpOutput().write(SHORT_BODY, 0, 5);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                resp.getHttpOutput().write(SHORT_BODY, 5, 3);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && TIMEOUT.equals(path)) {
                try {
                    Thread.sleep(5000);
                    resp.setStatus(200);
                    resp.getHttpOutput().write(SHORT_BODY);
                    resp.getHttpOutput().flush();
                    resp.getHttpOutput().complete(Callback.NOOP);
                } catch (InterruptedException e) {
                    throw new ServletException(e);
                }
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + path);
            }
        });

        testServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(testServer::stop));

        return testServer;
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
        LocalTestServer proxyServer = new LocalTestServer((req, resp, requestBody) -> {
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
