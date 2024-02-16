// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.test.http.LocalTestServer;
import org.eclipse.jetty.util.Callback;

import javax.servlet.ServletException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

/**
 * {@link LocalTestServer} used by all tests in this package.
 */
public final class NettyHttpClientLocalTestServer {
    private static volatile LocalTestServer server;
    private static final Semaphore SERVER_SEMAPHORE = new Semaphore(1);

    public static final String DEFAULT_PATH = "/default";
    public static final String PREBUILT_CLIENT_PATH = "/prebuiltClient";

    public static final String SHORT_BODY_PATH = "/short";
    public static final String LONG_BODY_PATH = "/long";
    public static final String ERROR_BODY_PATH = "/error";
    public static final String SHORT_POST_BODY_PATH = "/shortPost";
    public static final String SHORT_POST_BODY_WITH_VALIDATION_PATH = "/shortPostWithValidation";
    public static final String HTTP_HEADERS_PATH = "/httpHeaders";
    public static final String IO_EXCEPTION_PATH = "/ioException";
    public static final String NO_DOUBLE_UA_PATH = "/noDoubleUA";
    public static final String EXPECTED_HEADER = "userAgent";
    public static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";
    public static final String PROXY_TO_ADDRESS = "/proxyToAddress";

    public static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    public static final byte[] LONG_BODY = createLongBody();

    public static final HttpHeaderName TEST_HEADER = HttpHeaderName.fromString("testHeader");
    public static final String NULL_REPLACEMENT = "null";

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

            if (get && LONG_BODY_PATH.equals(path)) {
                resp.setStatus(200);
                resp.setContentLength(LONG_BODY.length);
                resp.setContentType("application/octet-stream");
                resp.getHttpOutput().write(LONG_BODY);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && DEFAULT_PATH.equals(path)) {
                resp.setStatus(200);
            } else if (get && PREBUILT_CLIENT_PATH.equals(path)) {
                boolean hasCookie = req.getCookies() != null
                    && Arrays.stream(req.getCookies())
                        .anyMatch(cookie -> "test".equals(cookie.getName()) && "success".equals(cookie.getValue()));

                // Mocked endpoint to test building a client with a set port.
                if (!hasCookie) {
                    throw new ServletException("Unexpected request: " + req.getMethod() + " " + path);
                }
            } else if (get && SHORT_BODY_PATH.equals(path)) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(SHORT_BODY.length);
                resp.getHttpOutput().write(SHORT_BODY);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && ERROR_BODY_PATH.equals(path)) {
                resp.setStatus(500);
                resp.setContentLength(5);
                resp.getHttpOutput().write("error".getBytes(StandardCharsets.UTF_8));
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (post && SHORT_POST_BODY_PATH.equals(path)) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(SHORT_BODY.length);
                resp.getHttpOutput().write(SHORT_BODY);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (post && SHORT_POST_BODY_WITH_VALIDATION_PATH.equals(path)) {
                if (!Objects.equals(ByteBuffer.wrap(LONG_BODY, 1, 42), ByteBuffer.wrap(requestBody))) {
                    resp.sendError(400, "Request body does not match expected value");
                }
            } else if (post && HTTP_HEADERS_PATH.equals(path)) {
                String headerNameString = TEST_HEADER.getCaseInsensitiveName();
                String responseTestHeaderValue = req.getHeader(headerNameString);
                if (responseTestHeaderValue == null) {
                    responseTestHeaderValue = NULL_REPLACEMENT;
                }

                resp.setHeader(headerNameString, responseTestHeaderValue);
            } else if (get && NO_DOUBLE_UA_PATH.equals(path)) {
                if (!EXPECTED_HEADER.equals(req.getHeader("User-Agent"))) {
                    resp.setStatus(400);
                }
            } else if (get && IO_EXCEPTION_PATH.equals(path)) {
                resp.getHttpChannel().getConnection().close();
            } else if (get && RETURN_HEADERS_AS_IS_PATH.equals(path)) {
                List<String> headerNames = Collections.list(req.getHeaderNames());
                headerNames.forEach(headerName -> {
                    List<String> headerValues = Collections.list(req.getHeaders(headerName));
                    headerValues.forEach(headerValue -> resp.addHeader(headerName, headerValue));
                });
            } else if (get && PROXY_TO_ADDRESS.equals(path)) {
                resp.setStatus(418);
                resp.getHttpOutput().write("I'm a teapot".getBytes(StandardCharsets.UTF_8));
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + path);
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

    private NettyHttpClientLocalTestServer() {
    }
}
