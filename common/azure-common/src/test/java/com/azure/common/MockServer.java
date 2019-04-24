// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common;


import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class MockServer {
    private static class TestHandler extends HandlerWrapper {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            LoggerFactory.getLogger(getClass()).info("Received request for " + baseRequest.getRequestURL());
            baseRequest.setHandled(true);
            Random random = new Random();

            byte[] buf = new byte[8192];
            InputStream is = request.getInputStream();
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            while (true) {
                int bytesRead = is.read(buf);
                if (bytesRead == -1) {
                    break;
                }
                md5.update(buf, 0, bytesRead);

                int randomNumber = random.nextInt(100000);
                if (randomNumber == 12345) {
                    LoggerFactory.getLogger(getClass()).info("Server had a transient error.");
                    response.setStatus(503);
                    response.getWriter().println("Error! Please try again.");

                    // Appears to be necessary to read all the request content to prevent hangs
                    // Would like to be able to test scenarios where the server drops the connection
                    // when we're in the middle of sending request content.
                    int bytes = is.read(buf);
                    while (bytes != -1) {
                        bytes = is.read(buf);
                    }

                    return;
                }
            }

            byte[] md5Digest = md5.digest();
            String encodedMD5 = Base64.getEncoder().encodeToString(md5Digest);
            if (request.getMethod().equals("DELETE")) {
                response.setStatus(202);
            } else {
                response.setStatus(201);
            }
            response.setHeader("Content-MD5", encodedMD5);
            LoggerFactory.getLogger(getClass()).info("Finished handling request " + baseRequest.getRequestURL());
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portString = System.getenv("JAVA_SDK_TEST_PORT");
        if (portString != null) {
            port = Integer.parseInt(portString, 10);
        }

        Server server = new Server(port);
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);

        String tempPath = System.getenv("JAVA_STRESS_TEST_TEMP_PATH");
        if (tempPath == null || tempPath.isEmpty()) {
            tempPath = "client-runtime/temp";
        }

        resourceHandler.setResourceBase(tempPath);
        ContextHandler ch = new ContextHandler("/javasdktest/upload");
        ch.setHandler(resourceHandler);

        HandlerList handlers = new HandlerList();
        handlers.addHandler(ch);
        handlers.addHandler(new TestHandler());

        server.setHandler(handlers);

        System.out.println("Starting MockServer");
        server.start();
        server.join();
        System.out.println("Shutting down MockServer");
    }
}
