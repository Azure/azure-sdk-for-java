// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.vertx;

import com.azure.core.http.test.common.LocalTestServer;
import org.eclipse.jetty.util.Callback;

import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@link LocalTestServer} used by all tests in this package.
 */
public final class VertxHttpClientLocalTestServer {
    private static volatile LocalTestServer server;
    private static final Semaphore SERVER_SEMAPHORE = new Semaphore(1);

    private static volatile LocalTestServer proxyServer;
    private static final Semaphore PROXY_SERVER_SEMAPHORE = new Semaphore(1);

    public static final String GET_ENDPOINT = "/get";
    public static final byte[] EXPECTED_GET_BYTES = new byte[32768];

    public static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    public static final byte[] LONG_BODY = createLongBody();
    public static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";
    public static final String TIMEOUT = "/timeout";

    public static final String PROXY_USERNAME = "foo";
    public static final String PROXY_PASSWORD = "bar";

    public static final String SERVICE_ENDPOINT = "/default";

    static {
        ThreadLocalRandom.current().nextBytes(EXPECTED_GET_BYTES);
    }

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
        LocalTestServer server = new LocalTestServer((req, resp, requestBody) -> {
            String path = req.getServletPath();
            boolean get = "GET".equalsIgnoreCase(req.getMethod());
            boolean post = "POST".equalsIgnoreCase(req.getMethod());

            if (get && GET_ENDPOINT.equals(req.getServletPath())) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(EXPECTED_GET_BYTES.length);
                resp.getHttpOutput().write(EXPECTED_GET_BYTES);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && SERVICE_ENDPOINT.equals(req.getServletPath())) {
                resp.setStatus(200);
            } else if (get && "/short".equals(path)) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(SHORT_BODY.length);
                resp.getHttpOutput().write(SHORT_BODY);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && "/long".equals(path)) {
                resp.setContentType("application/octet-stream");
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
                resp.setContentType("application/octet-stream");
                resp.setContentLength(SHORT_BODY.length);
                resp.getHttpOutput().write(SHORT_BODY);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && RETURN_HEADERS_AS_IS_PATH.equals(path)) {
                List<String> headerNames = Collections.list(req.getHeaderNames());
                headerNames.forEach(headerName -> {
                    List<String> headerValues = Collections.list(req.getHeaders(headerName));
                    headerValues.forEach(headerValue -> resp.addHeader(headerName, headerValue));
                });
            } else if (get && "/empty".equals(path)) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(0);
            } else if (get && "/connectionClose".equals(path)) {
                resp.getHttpChannel().getConnection().close();
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
                throw new ServletException("Unexpected request " + req.getMethod() + " " + path);
            }
        });

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        return server;
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

    private VertxHttpClientLocalTestServer() {
    }
}
