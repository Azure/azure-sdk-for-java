// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test;

import com.typespec.core.http.ContentType;
import com.typespec.core.test.http.HttpClientTests;
import com.typespec.core.test.http.LocalTestServer;
import com.typespec.core.test.implementation.entities.HttpBinFormDataJSON;
import com.typespec.core.test.implementation.entities.HttpBinJSON;
import com.typespec.core.test.utils.MessageDigestUtils;
import com.typespec.core.util.DateTimeRfc1123;
import com.typespec.core.util.serializer.JacksonAdapter;
import com.typespec.core.util.serializer.SerializerEncoding;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

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

/**
 * Server used when running {@link HttpClientTests}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class HttpClientTestsServer {
    private static final JacksonAdapter ADAPTER = new JacksonAdapter();
    private static final String PLAIN_RESPONSE = "/plainBytesNoHeader";
    private static final String HEADER_RESPONSE = "/plainBytesWithHeader";
    private static final String INVALID_HEADER_RESPONSE = "/plainBytesInvalidHeader";
    private static final String UTF_8_BOM_RESPONSE = "/utf8BomBytes";
    private static final String UTF_16BE_BOM_RESPONSE = "/utf16BeBomBytes";
    private static final String UTF_16LE_BOM_RESPONSE = "/utf16LeBomBytes";
    private static final String UTF_32BE_BOM_RESPONSE = "/utf32BeBomBytes";
    private static final String UTF_32LE_BOM_RESPONSE = "/utf32LeBomBytes";
    private static final String BOM_WITH_SAME_HEADER = "/bomBytesWithSameHeader";
    private static final String BOM_WITH_DIFFERENT_HEADER = "/bomBytesWithDifferentHeader";
    private static final String ECHO_RESPONSE = "/echo";

    private static final byte[] UTF_8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final byte[] UTF_16BE_BOM = {(byte) 0xFE, (byte) 0xFF};
    private static final byte[] UTF_16LE_BOM = {(byte) 0xFF, (byte) 0xFE};
    private static final byte[] UTF_32BE_BOM = {(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
    private static final byte[] UTF_32LE_BOM = {(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};

    private static final byte[] RETURN_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);

    public static LocalTestServer getHttpClientTestsServer() {
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
                resp.flushBuffer();
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
                resp.flushBuffer();
            } else if (get && PLAIN_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", RETURN_BYTES);
            } else if (get && HEADER_RESPONSE.equals(path)) {
                handleRequest(resp, "charset=UTF-16BE", RETURN_BYTES);
            } else if (get && INVALID_HEADER_RESPONSE.equals(path)) {
                handleRequest(resp, "charset=invalid", RETURN_BYTES);
            } else if (get && UTF_8_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_8_BOM));
            } else if (get && UTF_16BE_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_16BE_BOM));
            } else if (get && UTF_16LE_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_16LE_BOM));
            } else if (get && UTF_32BE_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_32BE_BOM));
            } else if (get && UTF_32LE_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_32LE_BOM));
            } else if (get && BOM_WITH_SAME_HEADER.equals(path)) {
                handleRequest(resp, "charset=UTF-8", addBom(UTF_8_BOM));
            } else if (get && BOM_WITH_DIFFERENT_HEADER.equals(path)) {
                handleRequest(resp, "charset=UTF-16", addBom(UTF_8_BOM));
            } else if (put && ECHO_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", requestBody);
            } else {
                throw new ServletException("Unexpected method: " + req.getMethod());
            }
        }, 100);
    }

    private static byte[] addBom(byte[] arr1) {
        byte[] mergedArray = new byte[arr1.length + RETURN_BYTES.length];

        System.arraycopy(arr1, 0, mergedArray, 0, arr1.length);
        System.arraycopy(RETURN_BYTES, 0, mergedArray, arr1.length, RETURN_BYTES.length);

        return mergedArray;
    }

    private static void handleRequest(HttpServletResponse response, String contentType, byte[] responseBody)
        throws IOException {
        response.setStatus(200);
        response.setContentType(contentType);
        response.setContentLength(responseBody.length);
        response.getOutputStream().write(responseBody);
        response.flushBuffer();
    }

    private static void sendBytesResponse(String urlPath, Response resp)
        throws IOException {
        int bodySize = Integer.parseInt(urlPath.split("/", 3)[2]);
        setBaseHttpHeaders(resp);
        resp.addHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM);
        resp.setContentLength(bodySize);

        byte[] body = new byte[bodySize];
        ThreadLocalRandom.current().nextBytes(body);

        resp.addHeader("ETag", MessageDigestUtils.md5(body));

        resp.getOutputStream().write(body);
        resp.flushBuffer();
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

        handleRequest(resp, "application/json", ADAPTER.serializeToBytes(responseBody, SerializerEncoding.JSON));
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

        handleRequest(resp, "application/json", ADAPTER.serializeToBytes(formBody, SerializerEncoding.JSON));
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
