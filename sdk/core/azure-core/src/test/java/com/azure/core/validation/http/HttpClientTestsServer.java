// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.validation.http;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.validation.http.models.HttpBinFormDataJson;
import com.azure.core.validation.http.models.HttpBinJson;
import com.azure.core.validation.http.models.PizzaSize;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
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

import static com.azure.core.validation.http.HttpValidatonUtils.md5;

/**
 * Server used when running {@link HttpClientTests}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public final class HttpClientTestsServer {
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
    public static final String ECHO_RESPONSE = "echo";
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
     * @return The {@link LocalTestServer}.
     */
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
                resp.getHttpOutput().flush();
            } else if (post && path.startsWith("/post")) {
                if ("application/x-www-form-urlencoded".equalsIgnoreCase(req.getHeader("Content-Type"))) {
                    sendFormResponse(resp, new String(requestBody, StandardCharsets.UTF_8));
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
                resp.getHttpOutput().write("void exception body thrown".getBytes(StandardCharsets.UTF_8));
                resp.getHttpOutput().flush();
                resp.getHttpOutput().complete(Callback.NOOP);
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
            } else if (get && pathMatches(path, HUGE_HEADER_RESPONSE)) {
                resp.addHeader(HUGE_HEADER_NAME.getCaseSensitiveName(), HUGE_HEADER_VALUE);
                resp.setStatus(200);
                resp.flushBuffer();
            } else {
                throw new ServletException("Unexpected method: " + req.getMethod());
            }
        }, 100);
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

    private static byte[] addBom(byte[] arr1) {
        byte[] mergedArray = new byte[arr1.length + RETURN_BYTES.length];

        System.arraycopy(arr1, 0, mergedArray, 0, arr1.length);
        System.arraycopy(RETURN_BYTES, 0, mergedArray, arr1.length, RETURN_BYTES.length);

        return mergedArray;
    }

    private static void handleRequest(Response response, String contentType, byte[] responseBody) throws IOException {
        response.setStatus(200);
        response.setContentType(contentType);
        response.setContentLength(responseBody.length);
        response.getHttpOutput().write(responseBody);
        response.getHttpOutput().flush();
        response.getHttpOutput().complete(Callback.NOOP);
    }

    private static void sendBytesResponse(String urlPath, Response resp) throws IOException {
        int bodySize = Integer.parseInt(urlPath.split("/", 3)[2]);
        setBaseHttpHeaders(resp);
        resp.addHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM);
        resp.setContentLength(bodySize);

        byte[] body = new byte[bodySize];
        ThreadLocalRandom.current().nextBytes(body);

        resp.addHeader("ETag", md5(body));

        resp.getHttpOutput().write(body);
        resp.getHttpOutput().flush();
        resp.getHttpOutput().complete(Callback.NOOP);
    }

    private static void sendSimpleHttpBinResponse(Request req, Response resp, String requestString) throws IOException {
        HttpBinJson responseBody = new HttpBinJson();
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

        handleRequest(resp, "application/json", responseBody.toJsonBytes());
    }

    private static void sendFormResponse(Response resp, String requestString) throws IOException {
        HttpBinFormDataJson formBody = new HttpBinFormDataJson();
        HttpBinFormDataJson.Form form = new HttpBinFormDataJson.Form();
        List<String> toppings = null;

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
                    form.pizzaSize(PizzaSize.fromString(kvpPieces[1]));
                    break;

                case "toppings":
                    if (toppings == null) {
                        toppings = new ArrayList<>();
                    }

                    toppings.add(kvpPieces[1]);
                    break;

                default:
                    break;
            }
        }

        form.toppings(toppings);
        formBody.form(form);

        handleRequest(resp, "application/json", formBody.toJsonBytes());
    }

    private static String cleanseUrl(HttpServletRequest req) {
        return (req.getQueryString() == null)
            ? req.getScheme() + "://" + req.getServerName() + req.getServletPath().replace("%20", " ")
            : req.getScheme() + "://" + req.getServerName() + req.getServletPath().replace("%20", " ") + "?"
                + req.getQueryString().replace("%20", " ");
    }

    private static void setBaseHttpHeaders(HttpServletResponse resp) {
        resp.addHeader("Date", new DateTimeRfc1123(OffsetDateTime.now(ZoneOffset.UTC)).toString());
        resp.addHeader("Connection", "keep-alive");
        resp.addHeader("X-Processed-Time", String.valueOf(ThreadLocalRandom.current().nextDouble(0.0D, 10.0D)));
        resp.addHeader("Access-Control-Allow-Credentials", "true");
        resp.addHeader("Content-Type", "application/json");
    }

    private HttpClientTestsServer() {
    }
}
