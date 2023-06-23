// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.http.HttpClientTests;
import com.azure.core.test.http.LocalTestServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Server used when running {@link HttpClientTests}.
 */
public class HttpClientTestsServer {
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
            boolean getRequest = "GET".equalsIgnoreCase(req.getMethod());
            boolean putRequest = "PUT".equalsIgnoreCase(req.getMethod());
            String path = req.getServletPath();

            if (getRequest && PLAIN_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", RETURN_BYTES);
            } else if (getRequest && HEADER_RESPONSE.equals(path)) {
                handleRequest(resp, "charset=UTF-16BE", RETURN_BYTES);
            } else if (getRequest && INVALID_HEADER_RESPONSE.equals(path)) {
                handleRequest(resp, "charset=invalid", RETURN_BYTES);
            } else if (getRequest && UTF_8_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_8_BOM));
            } else if (getRequest && UTF_16BE_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_16BE_BOM));
            } else if (getRequest && UTF_16LE_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_16LE_BOM));
            } else if (getRequest && UTF_32BE_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_32BE_BOM));
            } else if (getRequest && UTF_32LE_BOM_RESPONSE.equals(path)) {
                handleRequest(resp, "application/octet-stream", addBom(UTF_32LE_BOM));
            } else if (getRequest && BOM_WITH_SAME_HEADER.equals(path)) {
                handleRequest(resp, "charset=UTF-8", addBom(UTF_8_BOM));
            } else if (getRequest && BOM_WITH_DIFFERENT_HEADER.equals(path)) {
                handleRequest(resp, "charset=UTF-16", addBom(UTF_8_BOM));
            } else if (putRequest && ECHO_RESPONSE.equals(path)) {
                resp.setStatus(200);
                resp.setContentType("application/octet-stream");
                resp.setContentLength(requestBody.length);
                resp.getOutputStream().write(requestBody);
            } else {
                throw new ServletException("Unexpected method: " + req.getMethod());
            }
        }, 50);
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
        response.getOutputStream().write(responseBody);
    }
}
