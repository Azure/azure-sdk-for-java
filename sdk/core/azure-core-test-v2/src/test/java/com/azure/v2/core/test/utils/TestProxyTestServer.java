// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * A simple HTTP server for unit testing the test proxy infrastructure.
 */
public class TestProxyTestServer implements Closeable {
    private final Server server;
    private final ServerConnector httpConnector;

    private static final String TEST_JSON_RESPONSE_BODY = "{\"modelId\":\"0cd2728b-210e-4c05-b706-f70554276bcc\","
        + "\"createdDateTime\":\"2022-08-31T00:00:00Z\",\"apiVersion\":\"2022-08-31\","
        + "  \"accountKey\" : \"secret_account_key\"," + "  \"client_secret\" : \"secret_client_secret\"}";
    private static final String TEST_XML_RESPONSE_BODY = "{\"Body\":\"<UserDelegationKey>"
        + "<SignedTid>sensitiveInformation=</SignedTid></UserDelegationKey>\",\"primaryKey\":"
        + "\"<PrimaryKey>fakePrimaryKey</PrimaryKey>\", \"TableName\":\"listtable09bf2a3d\"}";

    /**
     * Constructor for TestProxyTestServer
     */
    public TestProxyTestServer() {
        this.server = new Server(new ExecutorThreadPool(10));

        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory();
        httpConnector = new ServerConnector(server, httpConnectionFactory);
        httpConnector.setHost("localhost");

        server.addConnector(httpConnector);

        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        ServletHolder servletHolder = new ServletHolder(new AzureTestHttpServlet((req, resp, requestBody) -> {
            String method = req.getMethod();
            if ("GET".equalsIgnoreCase(method)) {
                if ("/".equals(req.getRequestURI())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentLengthLong("hello world".length());
                    resp.getHttpOutput().write("hello world".getBytes(StandardCharsets.UTF_8));
                    resp.getHttpOutput().flush();
                } else if ("/echoheaders".equals(req.getRequestURI())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    Collections.list(req.getHeaderNames())
                        .forEach(headerName -> resp.addHeader(headerName, req.getHeader(headerName)));
                    resp.setContentLengthLong("echoheaders".length());
                    resp.getHttpOutput().write("echoheaders".getBytes(StandardCharsets.UTF_8));
                    resp.getHttpOutput().flush();
                } else if ("/fr/path/1".equals(req.getRequestURI())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    Collections.list(req.getHeaderNames())
                        .forEach(headerName -> resp.addHeader(headerName, req.getHeader(headerName)));
                    resp.setContentType("application/json");
                    resp.setHeader("Operation-Location",
                        "https://resourceInfo.cognitiveservices.azure.com/fr/models//905a58f9-131e-42b8-8410-493ab1517d62");
                    resp.setContentLengthLong(TEST_JSON_RESPONSE_BODY.length());
                    resp.getHttpOutput().write(TEST_JSON_RESPONSE_BODY.getBytes(StandardCharsets.UTF_8));
                    resp.getHttpOutput().flush();
                } else if ("/fr/path/2".equals(req.getRequestURI())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json");
                    resp.setContentLengthLong(TEST_XML_RESPONSE_BODY.length());
                    resp.getHttpOutput().write(TEST_XML_RESPONSE_BODY.getBytes(StandardCharsets.UTF_8));
                    resp.getHttpOutput().flush();
                } else if ("/getRedirect".equals(req.getRequestURI())) {
                    resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
                    resp.setContentType("application/json");
                    resp.setHeader("Location", "http://localhost:" + port() + "/echoheaders");
                    resp.getHttpOutput().flush();
                } else {
                    throw new RuntimeException(
                        "Uri doesn't match any routes. Uri: " + req.getRequestURI() + ", Method: GET");
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                if ("/first/path".equals(req.getRequestURI())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentLengthLong("first path".length());
                    resp.getHttpOutput().write("first path".getBytes(StandardCharsets.UTF_8));
                    resp.getHttpOutput().flush();
                } else if ("/post".equals(req.getRequestURI())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getHttpOutput().flush();
                } else {
                    throw new RuntimeException(
                        "Uri doesn't match any routes. Uri: " + req.getRequestURI() + ", Method: POST");
                }
            } else {
                throw new RuntimeException(
                    "Uri doesn't match any routes. Uri: " + req.getRequestURI() + ", Method: " + method);
            }
        }));

        servletContextHandler.addServlet(servletHolder, "/");

        try {
            server.start();
            while (!hasServerStarted(server)) {
                Thread.sleep(1000); // Wait until the server has actually started.
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final class AzureTestHttpServlet extends HttpServlet {
        private final RequestHandler requestHandler;

        private AzureTestHttpServlet(RequestHandler requestHandler) {
            this.requestHandler = requestHandler;
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            byte[] requestBody = fullyReadRequest(req.getInputStream());
            requestHandler.handle((Request) req, (Response) resp, requestBody);
        }
    }

    /**
     * Handler that will be used to process requests.
     */
    public interface RequestHandler {
        /**
         * Handles the request.
         *
         * @param req The request.
         * @param resp The response.
         * @param requestBody The request body.
         * @throws IOException If an IO error occurs.
         * @throws ServletException If a servlet error occurs.
         */
        void handle(Request req, Response resp, byte[] requestBody) throws IOException, ServletException;
    }

    private static byte[] fullyReadRequest(InputStream requestBody) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];
        int read;

        while ((read = requestBody.read(buffer, 0, buffer.length)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        return outputStream.toByteArray();
    }

    private static boolean hasServerStarted(Server server) {
        String serverState = server.getState();

        if (serverState.equals(AbstractLifeCycle.FAILED)
            || serverState.equals(AbstractLifeCycle.STOPPING)
            || serverState.equals(AbstractLifeCycle.STOPPED)) {
            throw new RuntimeException(
                "Server state has reached an unexpected state while waiting for it to start: " + serverState);
        }

        return serverState.equals(AbstractLifeCycle.STARTED) || serverState.equals(AbstractLifeCycle.RUNNING);
    }

    /**
     * Get the port of the server.
     *
     * @return The port of the server.
     */
    public int port() {
        return httpConnector.getLocalPort();
    }

    @Override
    public void close() {
        try {
            if (server.isRunning()) {
                server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
