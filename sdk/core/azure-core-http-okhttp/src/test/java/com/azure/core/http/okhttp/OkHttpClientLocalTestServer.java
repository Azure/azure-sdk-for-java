// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.okhttp;

import com.azure.core.validation.http.LocalTestServer;
import org.eclipse.jetty.util.Callback;

import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@link LocalTestServer} used by all tests in this package.
 */
public final class OkHttpClientLocalTestServer {
    private static volatile LocalTestServer server;
    private static final Semaphore SERVER_SEMAPHORE = new Semaphore(1);

    public static final String GET_ENDPOINT = "/get";
    public static final byte[] EXPECTED_GET_BYTES = new byte[32768];

    public static final String COOKIE_VALIDATOR_PATH = "/cookieValidator";
    public static final String DEFAULT_PATH = "/default";
    public static final String DISPATCHER_PATH = "/dispatcher";
    public static final String REDIRECT_PATH = "/redirect";
    public static final String LOCATION_PATH = "/location";
    public static final String TIMEOUT = "/timeout";

    public static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    public static final byte[] LONG_BODY = createLongBody();
    public static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";

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

            if ("GET".equalsIgnoreCase(req.getMethod()) && GET_ENDPOINT.equals(req.getServletPath())) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(EXPECTED_GET_BYTES.length);
                resp.getHttpOutput().write(EXPECTED_GET_BYTES);
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
            } else if (get && COOKIE_VALIDATOR_PATH.equals(path)) {
                boolean hasCookie = req.getCookies() != null
                    && Arrays.stream(req.getCookies())
                        .anyMatch(cookie -> "test".equals(cookie.getName()) && "success".equals(cookie.getValue()));
                if (!hasCookie) {
                    resp.setStatus(400);
                }
            } else if (get && DISPATCHER_PATH.equals(path)) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if (get && REDIRECT_PATH.equals(path)) {
                resp.setStatus(307);
                resp.setHeader("Location", req.getHeader("Location"));
            } else if (get && (DEFAULT_PATH.equals(path) || LOCATION_PATH.equals(path))) {
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

    private OkHttpClientLocalTestServer() {
    }
}
