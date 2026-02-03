// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.shared;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.utils.DateTimeRfc1123;
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

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * Server used when running {@link HttpClient tests}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class HttpClientTestsServer {
    private static final JsonSerializer SERIALIZER = new JsonSerializer();
    static final String PLAIN_RESPONSE = "plainBytesNoHeader";
    static final String HEADER_RESPONSE = "plainBytesWithHeader";
    static final String INVALID_HEADER_RESPONSE = "plainBytesInvalidHeader";
    static final String UTF_8_BOM_RESPONSE = "utf8BomBytes";
    static final String UTF_16BE_BOM_RESPONSE = "utf16BeBomBytes";
    static final String UTF_16LE_BOM_RESPONSE = "utf16LeBomBytes";
    static final String UTF_32BE_BOM_RESPONSE = "utf32BeBomBytes";
    static final String UTF_32LE_BOM_RESPONSE = "utf32LeBomBytes";
    static final String BOM_WITH_SAME_HEADER = "bomBytesWithSameHeader";
    static final String BOM_WITH_DIFFERENT_HEADER = "bomBytesWithDifferentHeader";
    static final String ECHO_RESPONSE = "echo";
    static final String SSE_RESPONSE = "serversentevent";
    static final String HUGE_HEADER_RESPONSE = "hugeHeader";

    private static final byte[] UTF_8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
    private static final byte[] UTF_16BE_BOM = { (byte) 0xFE, (byte) 0xFF };
    private static final byte[] UTF_16LE_BOM = { (byte) 0xFF, (byte) 0xFE };
    private static final byte[] UTF_32BE_BOM = { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF };
    private static final byte[] UTF_32LE_BOM = { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00 };

    private static final String HELLO_WORLD = "Hello World!";
    static final byte[] RETURN_BYTES = HELLO_WORLD.getBytes(StandardCharsets.UTF_8);
    static final HttpHeaderName HUGE_HEADER_NAME = HttpHeaderName.fromString("x-huge-header");
    static final String HUGE_HEADER_VALUE;

    static {
        // Create the huge header value, which is 1024 HELLO_WORLDs (about 12 KB).
        StringBuilder sb = new StringBuilder(HELLO_WORLD.length() * 1024);
        for (int i = 0; i < 1024; i++) {
            sb.append(HELLO_WORLD);
        }
        HUGE_HEADER_VALUE = sb.toString();
    }

    /**
     * Gets the {@link LocalTestServer}.
     *
     * @param supportedProtocol The protocol supported by this server. If null, {@link HttpProtocolVersion#HTTP_1_1}
     * will be the supported protocol.
     * @param includeTls Flag indicating if TLS will be included.
     * @return The {@link LocalTestServer}.
     */
    public static LocalTestServer getHttpClientTestsServer(HttpProtocolVersion supportedProtocol, boolean includeTls) {
        return new LocalTestServer(supportedProtocol, includeTls, (req, resp, requestBody) -> {
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
                // Stub that will return a response with a body that contains the URI string as-is.
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
            } else if (get && pathMatches(path, PLAIN_RESPONSE)) {
                handleRequest(resp, "application/octet-stream", RETURN_BYTES);
            } else if (get && pathMatches(path, HEADER_RESPONSE)) {
                handleRequest(resp, "charset=UTF-16BE", RETURN_BYTES);
            } else if (get && pathMatches(path, INVALID_HEADER_RESPONSE)) {
                handleRequest(resp, "charset=invalid", RETURN_BYTES);
            } else if (get && pathMatches(path, UTF_8_BOM_RESPONSE)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_8_BOM));
            } else if (get && pathMatches(path, UTF_16BE_BOM_RESPONSE)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_16BE_BOM));
            } else if (get && pathMatches(path, UTF_16LE_BOM_RESPONSE)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_16LE_BOM));
            } else if (get && pathMatches(path, UTF_32BE_BOM_RESPONSE)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_32BE_BOM));
            } else if (get && pathMatches(path, UTF_32LE_BOM_RESPONSE)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_32LE_BOM));
            } else if (get && pathMatches(path, BOM_WITH_SAME_HEADER)) {
                handleRequest(resp, "charset=UTF-8", addBom(UTF_8_BOM));
            } else if (get && pathMatches(path, BOM_WITH_DIFFERENT_HEADER)) {
                handleRequest(resp, "charset=UTF-16", addBom(UTF_8_BOM));
            } else if (put && pathMatches(path, ECHO_RESPONSE)) {
                handleRequest(resp, "application/octet-stream", requestBody);
            } else if (get && pathMatches(path, SSE_RESPONSE)) {
                if (req.getHeader("Last-Event-Id") != null) {
                    sendSSELastEventIdResponse(resp);
                } else {
                    sendSSEResponseWithRetry(resp);
                }
            } else if (post && pathMatches(path, SSE_RESPONSE)) {
                sendSSEResponseWithDataOnly(resp);
            } else if (put && pathMatches(path, SSE_RESPONSE)) {
                resp.addHeader("Content-Type", ContentType.TEXT_EVENT_STREAM);
                resp.setStatus(200);
                resp.getOutputStream().write(("msg hello world \n\n").getBytes());
                resp.flushBuffer();
            } else if (get && pathMatches(path, HUGE_HEADER_RESPONSE)) {
                resp.addHeader(HUGE_HEADER_NAME.getCaseSensitiveName(), HUGE_HEADER_VALUE);
                resp.setContentLength(0);
                resp.setStatus(200);
                resp.flushBuffer();
            } else {
                throw new ServletException("Unexpected request " + req.getMethod() + " " + path);
            }
        });
    }

    /**
     * Helper method to check if the path matches the test path.
     * <p>
     * All path constants in this class don't contain a leading slash to allow them to be used in
     * {@link HttpClientTests}. This method checks that the path matches the test path by inferring the leading slash
     * on the test path.
     *
     * @param path The path used in the request.
     * @param testPathWithoutLeadingSlash The test path without leading slash.
     * @return Whether the path matches the test path.
     */
    private static boolean pathMatches(String path, String testPathWithoutLeadingSlash) {
        // Check that the path starts with a leading slash, that the length of the path is equal to the test path
        // length + 1 (for the leading slash), and that the test path matches the path starting from index 1.
        return path.charAt(0) == '/'
            && path.length() == testPathWithoutLeadingSlash.length() + 1
            && testPathWithoutLeadingSlash.regionMatches(0, path, 1, testPathWithoutLeadingSlash.length());
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

    private static void sendBytesResponse(String uriPath, Response resp) throws IOException {
        int bodySize = Integer.parseInt(uriPath.split("/", 3)[2]);
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

        responseBody.uri(cleanseUri(req));
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

    private static String cleanseUri(HttpServletRequest req) {
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
