// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.http;

import com.azure.v2.core.test.implementation.models.HttpBinFormDataJson;
import com.azure.v2.core.test.implementation.models.HttpBinJson;
import com.azure.v2.core.test.implementation.models.PizzaSize;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.utils.Base64Uri;
import io.clientcore.core.utils.DateTimeRfc1123;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This HttpClient attempts to mimic the behavior of http://httpbin.org without ever making a network call.
 */
public class MockHttpClient extends NoOpHttpClient {
    private static final HttpHeaders RESPONSE_HEADERS
        = new HttpHeaders().set(HttpHeaderName.DATE, "Fri, 13 Oct 2017 20:33:09 GMT")
            .set(HttpHeaderName.VIA, "1.1 via")
            .set(HttpHeaderName.CONNECTION, "keep-alive")
            .set(HttpHeaderName.fromString("X-Processed-Time"), "1.0")
            .set(HttpHeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
            .set(HttpHeaderName.CONTENT_TYPE, "application/json");

    /**
     * Creates a new instance of MockHttpClient.
     */
    public MockHttpClient() {
    }

    @Override
    public Response<BinaryData> send(HttpRequest request) {

        Response<BinaryData> response = null;
        final URI requestUri = request.getUri();
        final String requestHost = requestUri.getHost();
        final String contentType = request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);
        if ("localhost".equalsIgnoreCase(requestHost)) {
            final String requestPath = requestUri.getPath();
            final String requestPathLower = requestPath.toLowerCase(Locale.ROOT);
            if (requestPathLower.startsWith("/anything")) {
                if ("HEAD".equals(request.getHttpMethod().name())) {
                    response = new MockHttpResponse(request, 200, new byte[0]);
                } else {
                    final HttpBinJson json = new HttpBinJson();
                    json.uri(cleanseUri(requestUri));
                    json.headers(toMap(request.getHeaders()));
                    response = new MockHttpResponse(request, 200, toJsonBytes(json));
                }
            } else if (requestPathLower.startsWith("/bytes/")) {
                final String byteCountString = requestPath.substring("/bytes/".length());
                final int byteCount = Integer.parseInt(byteCountString);
                HttpHeaders newHeaders
                    = new HttpHeaders(RESPONSE_HEADERS).set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream")
                        .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(byteCount));
                byte[] content;
                if (byteCount > 0) {
                    content = new byte[byteCount];
                    ThreadLocalRandom.current().nextBytes(content);
                    newHeaders = newHeaders.set(HttpHeaderName.ETAG, md5(content));
                } else {
                    content = null;
                }
                response = new MockHttpResponse(request, 200, newHeaders, content);
            } else if (requestPathLower.startsWith("/base64urlbytes/")) {
                final String byteCountString = requestPath.substring("/base64urlbytes/".length());
                final int byteCount = Integer.parseInt(byteCountString);
                final byte[] bytes = new byte[byteCount];
                for (int i = 0; i < byteCount; ++i) {
                    bytes[i] = (byte) i;
                }
                final Base64Uri base64EncodedBytes = bytes.length == 0 ? null : Base64Uri.encode(bytes);
                response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, base64EncodedBytes);
            } else if ("/base64urllistofbytes".equals(requestPathLower)) {
                final List<String> base64EncodedBytesList = new ArrayList<>();
                for (int i = 0; i < 3; ++i) {
                    final int byteCount = (i + 1) * 10;
                    final byte[] bytes = new byte[byteCount];
                    for (int j = 0; j < byteCount; ++j) {
                        bytes[j] = (byte) j;
                    }
                    final Base64Uri base64UriEncodedBytes = Base64Uri.encode(bytes);
                    base64EncodedBytesList.add(base64UriEncodedBytes.toString());
                }
                response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, base64EncodedBytesList);
            } else if ("/base64urllistoflistofbytes".equals(requestPathLower)) {
                final List<List<String>> result = new ArrayList<>();
                for (int i = 0; i < 2; ++i) {
                    final List<String> innerList = new ArrayList<>();
                    for (int j = 0; j < (i + 1) * 2; ++j) {
                        final int byteCount = (j + 1) * 5;
                        final byte[] bytes = new byte[byteCount];
                        for (int k = 0; k < byteCount; ++k) {
                            bytes[k] = (byte) k;
                        }

                        final Base64Uri base64UriEncodedBytes = Base64Uri.encode(bytes);
                        innerList.add(base64UriEncodedBytes.toString());
                    }
                    result.add(innerList);
                }
                response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, result);
            } else if ("/base64urlmapofbytes".equals(requestPathLower)) {
                final Map<String, String> result = new HashMap<>();
                for (int i = 0; i < 2; ++i) {
                    final String key = Integer.toString(i);

                    final int byteCount = (i + 1) * 10;
                    final byte[] bytes = new byte[byteCount];
                    for (int j = 0; j < byteCount; ++j) {
                        bytes[j] = (byte) j;
                    }

                    final Base64Uri base64UriEncodedBytes = Base64Uri.encode(bytes);
                    result.put(key, base64UriEncodedBytes.toString());
                }
                response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, result);
            } else if ("/datetimerfc1123".equals(requestPathLower)) {
                final DateTimeRfc1123 now
                    = new DateTimeRfc1123(OffsetDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC));
                final String result = now.toString();
                response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, result);
            } else if ("/unixtime".equals(requestPathLower)) {
                response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, 0);
            } else if ("/delete".equals(requestPathLower)) {
                final HttpBinJson json = new HttpBinJson();
                json.uri(cleanseUri(requestUri));
                json.data(createHttpBinResponseDataForRequest(request));
                response = new MockHttpResponse(request, 200, toJsonBytes(json));
            } else if ("/get".equals(requestPathLower)) {
                final HttpBinJson json = new HttpBinJson();
                json.uri(cleanseUri(requestUri));
                json.headers(toMap(request.getHeaders()));
                response = new MockHttpResponse(request, 200, toJsonBytes(json));
            } else if ("/patch".equals(requestPathLower)) {
                final HttpBinJson json = new HttpBinJson();
                json.uri(cleanseUri(requestUri));
                json.data(createHttpBinResponseDataForRequest(request));
                response = new MockHttpResponse(request, 200, toJsonBytes(json));
            } else if ("/post".equals(requestPathLower)) {
                if (contentType != null && contentType.contains("x-www-form-urlencoded")) {
                    Map<String, String> parsed = bodyToMap(request);
                    final HttpBinFormDataJson json = new HttpBinFormDataJson();
                    HttpBinFormDataJson.Form form = new HttpBinFormDataJson.Form();
                    form.customerName(parsed.get("custname"));
                    form.customerEmail(parsed.get("custemail"));
                    form.customerTelephone(parsed.get("custtel"));
                    form.pizzaSize(PizzaSize.valueOf(parsed.get("size")));
                    form.toppings(Arrays.asList(parsed.get("toppings").split(",")));
                    json.form(form);
                    response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, toJsonBytes(json));
                } else {
                    final HttpBinJson json = new HttpBinJson();
                    json.uri(cleanseUri(requestUri));
                    json.data(createHttpBinResponseDataForRequest(request));
                    json.headers(toMap(request.getHeaders()));
                    response = new MockHttpResponse(request, 200, toJsonBytes(json));
                }
            } else if ("/put".equals(requestPathLower)) {
                final HttpBinJson json = new HttpBinJson();
                json.uri(cleanseUri(requestUri));
                json.data(createHttpBinResponseDataForRequest(request));
                json.headers(toMap(request.getHeaders()));
                response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, toJsonBytes(json));
            } else if (requestPathLower.startsWith("/status/")) {
                final String statusCodeString = requestPathLower.substring("/status/".length());
                final int statusCode = Integer.parseInt(statusCodeString);
                response = new MockHttpResponse(request, statusCode);
            } else if (requestPathLower.startsWith("/voideagerreadoom")) {
                response = new MockHttpResponse(request, 200);
            } else if (requestPathLower.startsWith("/voiderrorreturned")) {
                response
                    = new MockHttpResponse(request, 400, "void exception body thrown".getBytes(StandardCharsets.UTF_8));
            }
        } else if ("echo.org".equalsIgnoreCase(requestHost)) {
            return new MockHttpResponse(request, 200, new HttpHeaders(request.getHeaders()), request.getBody());
        }

        if (response == null) {
            response = new MockHttpResponse(request, 500);
        }

        return response;
    }

    private static String createHttpBinResponseDataForRequest(HttpRequest request) {
        return bodyToString(request);
    }

    private static String bodyToString(HttpRequest request) {
        String body = "";
        if (request.getBody() != null) {
            return request.getBody().toString();
        }
        return body;
    }

    private static Map<String, List<String>> toMap(HttpHeaders headers) {
        final Map<String, List<String>> result = new HashMap<>();
        headers.stream().forEach(header -> result.put(header.getName().getCaseSensitiveName(), header.getValues()));
        return result;
    }

    private static Map<String, String> bodyToMap(HttpRequest request) {
        final Map<String, String> result = new HashMap<>();
        String body = bodyToString(request);
        for (String keyValPair : body.split("&")) {
            String[] parts = keyValPair.split("=");
            assert parts.length == 2;
            if (result.containsKey(parts[0])) {
                result.put(parts[0], result.get(parts[0]) + "," + parts[1]);
            } else {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }

    private static String cleanseUri(URI uri) {
        StringBuilder builder = new StringBuilder();
        builder.append(uri.getScheme()).append("://").append(uri.getHost()).append(uri.getPath().replace("%20", " "));

        if (uri.getQuery() != null) {
            builder.append("?").append(uri.getQuery().replace("%20", " "));
        }

        return builder.toString();
    }

    /**
     * Returns base64 encoded MD5 of bytes.
     *
     * @param bytes bytes.
     * @return base64 encoded MD5 of bytes.
     * @throws RuntimeException if md5 is not found.
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

    private byte[] toJsonBytes(JsonSerializable<?> json) {
        try {
            return json.toJsonBytes();
        } catch (IOException ex) {
            throw CoreException.from(ex);
        }
    }
}
