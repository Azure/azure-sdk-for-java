// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.http.ContentType;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.implementation.entities.HttpBinFormDataJSON;
import com.azure.core.test.implementation.entities.HttpBinJSON;
import com.azure.core.test.utils.MessageDigestUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class RestProxyTestsServer {
    private static final JacksonAdapter JACKSON_ADAPTER = new JacksonAdapter();

    public static LocalTestServer getRestProxyTestsServer() {
        return new LocalTestServer((req, resp, requestBody) -> {
            String path = req.getServletPath();
            boolean get = "GET".equalsIgnoreCase(req.getMethod());
            boolean post = "POST".equalsIgnoreCase(req.getMethod());
            boolean put = "PUT".equalsIgnoreCase(req.getMethod());
            boolean head = "HEAD".equalsIgnoreCase(req.getMethod());
            boolean delete = "DELETE".equalsIgnoreCase(req.getMethod());
            boolean patch = "PATCH".equalsIgnoreCase(req.getMethod());

            if (get && path.startsWith("/bytes")) {
                // Stub that will return a response with a body containing the passed number of bytes.
                sendBytesResponse(path, resp);
            } else if (get && path.startsWith("/status")) {
                // Stub that will return a response with the passed status code.
                resp.setStatus(Integer.parseInt(path.split("/", 3)[2]));
            } else if (post && path.startsWith("/post")) {
                if ("application/x-www-form-urlencoded".equalsIgnoreCase(req.getHeader("Content-Type"))) {
                    sendFormResponse(req, resp, new String(requestBody, StandardCharsets.UTF_8));
                } else {
                    sendSimpleHttpBinResponse(req, resp, new String(requestBody, StandardCharsets.UTF_8));
                }
            } else if (((get || head) && path.startsWith("/anything"))
                || (put && path.startsWith("/put"))
                || (delete && path.startsWith("/delete"))
                || (patch && path.startsWith("/patch"))
                || (get && path.startsWith("/get"))) {
                // Stub that will return a response with a body that contains the URL string as-is.
                sendSimpleHttpBinResponse(req, resp, new String(requestBody, StandardCharsets.UTF_8));
            } else if (head && path.startsWith("/voideagerreadoom")) {
                // Validates a bug where a void, or Void, response type would previously attempt to eagerly read the
                // response body. This resulted in OutOfMemoryErrors or high memory usage in APIs such as the
                // getProperties on Blobs, Datalake, and Files where the size of the resource is the Content-Length
                // header value. So, there could be an attempt to create a byte[] large enough to hold the response.
                //
                // This uses a size too large for a byte[], so if the incorrect handling is used an OutOfMemoryError
                // will be thrown.
                resp.setHeader("Content-Length", "10737418240"); // 10 GB
            } else if (put && path.startsWith("/voiderrorreturned")) {
                resp.setStatus(400);
                resp.getOutputStream().write("void exception body thrown".getBytes(StandardCharsets.UTF_8));
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + req.getServletPath());
            }
        }, 20);
    }

    private static void sendBytesResponse(String urlPath, HttpServletResponse resp)
        throws IOException {
        int bodySize = Integer.parseInt(urlPath.split("/", 3)[2]);
        setBaseHttpHeaders(resp);
        resp.addHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM);
        resp.setContentLength(bodySize);

        byte[] body = new byte[bodySize];
        ThreadLocalRandom.current().nextBytes(body);

        resp.addHeader("ETag", MessageDigestUtils.md5(body));

        resp.getOutputStream().write(body);
    }

    private static void sendSimpleHttpBinResponse(HttpServletRequest req, HttpServletResponse resp,
        String requestString) throws IOException {
        HttpBinJSON responseBody = new HttpBinJSON();
        responseBody.url(cleanseUrl(req));

        responseBody.data(requestString);

        if (req.getHeaderNames().hasMoreElements()) {
            Map<String, List<String>> headers = new HashMap<>();

            List<String> headerNames = Collections.list(req.getHeaderNames());
            headerNames.forEach(headerName -> {
                List<String> headerValues = Collections.list(req.getHeaders(headerName));
                headers.put(headerName, headerValues);
                headerValues.forEach(headerValue -> resp.addHeader(headerName, headerValue));
            });

            setBaseHttpHeaders(resp);
            responseBody.headers(headers);
        }

        byte[] responseBodyBytes = JACKSON_ADAPTER.serializeToBytes(responseBody, SerializerEncoding.JSON);
        resp.setContentLength(responseBodyBytes.length);
        resp.setContentType("application/json");
        resp.getOutputStream().write(responseBodyBytes);
    }

    private static void sendFormResponse(HttpServletRequest req, HttpServletResponse resp, String requestString)
        throws IOException {
        HttpBinFormDataJSON formBody = new HttpBinFormDataJSON();
        HttpBinFormDataJSON.Form form = new HttpBinFormDataJSON.Form();
        List<String> toppings = new ArrayList<>();

        for (String formKvp : requestString.split("&")) {
            String[] kvpPieces = formKvp.split("=");

            switch (kvpPieces[0]) {
                case "custname":
                    form.customerName(kvpPieces[1]);
                    break;
                case "custtel":
                    form.customerTelephone(kvpPieces[1]);
                    break;
                case "custemail":
                    form.customerEmail(kvpPieces[1]);
                    break;
                case "size":
                    form.pizzaSize(HttpBinFormDataJSON.PizzaSize.valueOf(kvpPieces[1]));
                    break;
                case "toppings":
                    toppings.add(kvpPieces[1]);
                    break;
                default:
                    break;
            }
        }

        form.toppings(toppings);
        formBody.form(form);

        byte[] responseBodyBytes = JACKSON_ADAPTER.serializeToBytes(formBody, SerializerEncoding.JSON);
        resp.setContentLength(responseBodyBytes.length);
        resp.setContentType("application/json");
        resp.getOutputStream().write(responseBodyBytes);
    }

    private static String cleanseUrl(HttpServletRequest req) {
        StringBuilder builder = new StringBuilder();
        builder.append(req.getScheme())
            .append("://")
            .append(req.getServerName())
            .append(req.getServletPath().replace("%20", " "));

        if (req.getQueryString() != null) {
            builder.append("?").append(req.getQueryString().replace("%20", " "));
        }

        return builder.toString();
    }

    private static void setBaseHttpHeaders(HttpServletResponse resp) {
        resp.addHeader("Date", new DateTimeRfc1123(OffsetDateTime.now(ZoneOffset.UTC)).toString());
        resp.addHeader("Connection", "keep-alive");
        resp.addHeader("X-Processed-Time", String.valueOf(Math.random() * 10));
        resp.addHeader("Access-Control-Allow-Credentials", "true");
        resp.addHeader("Content-Type", "application/json");
    }
}
