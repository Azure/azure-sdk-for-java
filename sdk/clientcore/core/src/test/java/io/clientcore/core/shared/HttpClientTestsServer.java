// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.shared;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ContentType;
import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import io.clientcore.core.util.serializer.ObjectSerializer;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;

/**
 * Server used when running {@link HttpClient tests}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class HttpClientTestsServer {
    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();
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
    private static final String SSE_RESPONSE = "/serversentevent";

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
                sendSimpleHttpBinResponse(req, resp, new String(requestBody, StandardCharsets.UTF_8),
                    "application/json");
            } else if (post && path.startsWith("/stream")) {
                sendSimpleHttpBinResponse(req, resp, new String(requestBody, StandardCharsets.UTF_8),
                    "application/octet-stream");
            } else if (((get || head) && path.startsWith("/anything"))
                || (put && path.startsWith("/put"))
                || (delete && path.startsWith("/delete"))
                || (patch && path.startsWith("/patch"))
                || (get && path.startsWith("/get"))) {
                // Stub that will return a response with a body that contains the URL string as-is.
                sendSimpleHttpBinResponse(req, resp, new String(requestBody, StandardCharsets.UTF_8),
                    "application/json");
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
        }, 100);
    }

    private static void sendSSEResponseWithDataOnly(Response resp) throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(("data: YHOO\n" + "data: +2\n" + "data: 10\n" + "\n").getBytes());
        resp.flushBuffer();
    }

    private static String addServerSentEventWithRetry() {
        return ": test stream\n" + "data: first event\n" + "id: 1\n" + "retry: 100\n\n"
            + "data: This is the second message, it\n" + "data: has two lines.\n" + "id: 2\n\n" + "data:  third event";
    }

    private static void sendSSEResponseWithRetry(Response resp) throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(addServerSentEventWithRetry().getBytes());
        resp.flushBuffer();
    }

    private static String addServerSentEventLast() {
        return "data: This is the second message, it\n" + "data: has two lines.\n" + "id: 2\n\n" + "data:  third event";
    }

    private static void sendSSELastEventIdResponse(Response resp) throws IOException {
        resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
        resp.getOutputStream().write(addServerSentEventLast().getBytes());
        resp.flushBuffer();
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

        resp.addHeader("ETag", md5(body));

        resp.getOutputStream().write(body);
        resp.flushBuffer();
    }

    private static void sendSimpleHttpBinResponse(HttpServletRequest req, HttpServletResponse resp,
                                                  String requestString, String contentType) throws IOException {
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

        if (!isNullOrEmpty(req.getQueryString())) {
            Map<String, List<String>> queryParams = parseQueryParams(req);

            responseBody.queryParams(queryParams);
        }

        handleRequest(resp, contentType, SERIALIZER.serializeToBytes(responseBody));
    }

    private static Map<String, List<String>> parseQueryParams(HttpServletRequest req) {
        String[] queryParams = req.getQueryString().split("&");
        Map<String, List<String>> queryParamsMap = new HashMap<>();

        for (String queryParam : queryParams) {
            final String[] queryParamParts = queryParam.split("=");
            final String paramName = queryParamParts[0];
            final String paramValue = queryParamParts.length == 2 ? queryParamParts[1] : null;

            List<String> currentValues = queryParamsMap.get(paramName);

            if (!isNullOrEmpty(paramValue)) {
                if (currentValues == null) {
                    currentValues = new ArrayList<>();
                }

                currentValues.add(paramValue);

                queryParamsMap.put(paramName, currentValues);
            } else {
                queryParamsMap.put(paramName, null);
            }
        }

        return queryParamsMap;
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

    /**
     * Returns base64 encoded MD5 of bytes.
     *
     * @param bytes Bytes.
     *
     * @return base64 Encoded MD5 of bytes.
     *
     * @throws RuntimeException If md5 is not found.
     */
    public static String md5(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
