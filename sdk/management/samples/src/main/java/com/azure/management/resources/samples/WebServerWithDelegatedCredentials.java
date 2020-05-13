// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

//
//package com.azure.management.resources.samples;
//
//import com.microsoft.azure.credentials.DelegatedTokenCredentials;
//import com.microsoft.azure.management.Azure;
//import com.microsoft.azure.management.graphrbac.ServicePrincipal;
//import com.sun.net.httpserver.HttpContext;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import com.sun.net.httpserver.HttpServer;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Azure Resource sample for delegated credentials.
// * The application behind the auth file must have "http://localhost:8000"
// * as a valid reply URL.
// *
// * - Creates a web server at http://localhost:8000
// * - User opens a browser to authenticate
// * - Lists service principals
// */
//
//public final class WebServerWithDelegatedCredentials {
//    /**
//     * Main function which runs the actual sample.
//     * @param authFile the auth file backing the web server
//     * @return true if sample runs successfully
//     * @throws Exception exceptions running the server
//     */
//    public static boolean runSample(File authFile) throws Exception {
//        final String redirectUrl = "http://localhost:8000";
//        final ExecutorService executor = Executors.newCachedThreadPool();
//
//        try {
//            DelegatedTokenCredential credentials = DelegatedTokenCredential.fromFile(authFile, redirectUrl);
//
//            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
//            HttpContext context = server.createContext("/", new MyHandler());
//            context.getAttributes().put("credentials", credentials);
//            server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
//            server.start();
//
//            // Use a browser to login within a minute
//            Thread.sleep(60000);
//            return true;
//        } finally {
//            executor.shutdown();
//        }
//    }
//
//    private static class MyHandler implements HttpHandler {
//        @Override
//        public void handle(HttpExchange t) throws IOException {
//            try {
//                String url = t.getRequestURI().toString();
//                String code = null;
//                if (url != null && !url.isEmpty()) {
//                    for (String query : url.split("[?&]")) {
//                        if (query.startsWith("code=")) {
//                            code = query.replace("code=", "");
//                        }
//                    }
//                }
//                String response;
//                DelegatedTokenCredentials credentials = (DelegatedTokenCredentials) t.getHttpContext().getAttributes().get("credentials");
//                if (code == null) {
//                    response = String.format("<a href='%s'>Login</a>", credentials.generateAuthenticationUrl());
//                } else {
//                    credentials.setAuthorizationCode(code);
//                    Azure.Authenticated client = Azure.authenticate(credentials);
//                    response = "Service principals in this tenant:\n";
//                    for (ServicePrincipal servicePrincipal : client.servicePrincipals().list()) {
//                        response += (servicePrincipal.name() + " - " + servicePrincipal.applicationId() + "\n");
//                    }
//                }
//                t.sendResponseHeaders(200, response.length());
//                OutputStream os = t.getResponseBody();
//                os.write(response.getBytes());
//                os.close();
//            } catch (Exception e) {
//                String response = "Failed: " + e.getMessage();
//                t.sendResponseHeaders(500, response.length());
//                OutputStream os = t.getResponseBody();
//                os.write(response.getBytes());
//                os.close();
//            }
//        }
//    }
//
//    /**
//     * Main entry point.
//     *
//     * @param args the parameters
//     */
//    public static void main(String[] args) {
//        try {
//            //=================================================================
//            // Authenticate
//
//            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
//            runSample(credFile);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}
