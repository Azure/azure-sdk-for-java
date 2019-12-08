// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.test.implementation.entities.HttpBinFormDataJSON;
import com.azure.core.test.implementation.entities.HttpBinFormDataJSON.Form;
import com.azure.core.test.implementation.entities.HttpBinFormDataJSON.PizzaSize;
import com.azure.core.test.implementation.entities.HttpBinJSON;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Base64Url;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * This HttpClient attempts to mimic the behavior of http://httpbin.org without ever making a network call.
 */
public class MockHttpClient extends NoOpHttpClient {
    private static final HttpHeaders RESPONSE_HEADERS = new HttpHeaders()
            .put("Date", "Fri, 13 Oct 2017 20:33:09 GMT")
            .put("Via", "1.1 vegur")
            .put("Connection", "keep-alive")
            .put("X-Processed-Time", "1.0")
            .put("Access-Control-Allow-Credentials", "true")
            .put("Content-Type", "application/json");

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        HttpResponse response = null;

        try {
            final URL requestUrl = request.getUrl();
            final String requestHost = requestUrl.getHost();
            final String contentType = request.getHeaders().getValue("Content-Type");
            if ("httpbin.org".equalsIgnoreCase(requestHost)) {
                final String requestPath = requestUrl.getPath();
                final String requestPathLower = requestPath.toLowerCase();
                if (requestPathLower.equals("/anything") || requestPathLower.startsWith("/anything/")) {
                    if ("HEAD".equals(request.getHttpMethod())) {
                        response = new MockHttpResponse(request, 200, new byte[0]);
                    } else {
                        final HttpBinJSON json = new HttpBinJSON();
                        json.url(request.getUrl().toString()
                                // This is just to mimic the behavior we've seen with httpbin.org.
                                .replace("%20", " "));
                        json.headers(toMap(request.getHeaders()));
                        response = new MockHttpResponse(request, 200, json);
                    }
                } else if (requestPathLower.startsWith("/bytes/")) {
                    final String byteCountString = requestPath.substring("/bytes/".length());
                    final int byteCount = Integer.parseInt(byteCountString);
                    HttpHeaders newHeaders = new HttpHeaders(RESPONSE_HEADERS)
                            .put("Content-Type", "application/octet-stream")
                            .put("Content-Length", Integer.toString(byteCount));
                    response = new MockHttpResponse(request, 200, newHeaders, byteCount == 0 ? null : new byte[byteCount]);
                } else if (requestPathLower.startsWith("/base64urlbytes/")) {
                    final String byteCountString = requestPath.substring("/base64urlbytes/".length());
                    final int byteCount = Integer.parseInt(byteCountString);
                    final byte[] bytes = new byte[byteCount];
                    for (int i = 0; i < byteCount; ++i) {
                        bytes[i] = (byte) i;
                    }
                    final Base64Url base64EncodedBytes = bytes.length == 0 ? null : Base64Url.encode(bytes);
                    response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, base64EncodedBytes);
                } else if (requestPathLower.equals("/base64urllistofbytes")) {
                    final List<String> base64EncodedBytesList = new ArrayList<>();
                    for (int i = 0; i < 3; ++i) {
                        final int byteCount = (i + 1) * 10;
                        final byte[] bytes = new byte[byteCount];
                        for (int j = 0; j < byteCount; ++j) {
                            bytes[j] = (byte) j;
                        }
                        final Base64Url base64UrlEncodedBytes = Base64Url.encode(bytes);
                        base64EncodedBytesList.add(base64UrlEncodedBytes.toString());
                    }
                    response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, base64EncodedBytesList);
                } else if (requestPathLower.equals("/base64urllistoflistofbytes")) {
                    final List<List<String>> result = new ArrayList<>();
                    for (int i = 0; i < 2; ++i) {
                        final List<String> innerList = new ArrayList<>();
                        for (int j = 0; j < (i + 1) * 2; ++j) {
                            final int byteCount = (j + 1) * 5;
                            final byte[] bytes = new byte[byteCount];
                            for (int k = 0; k < byteCount; ++k) {
                                bytes[k] = (byte) k;
                            }

                            final Base64Url base64UrlEncodedBytes = Base64Url.encode(bytes);
                            innerList.add(base64UrlEncodedBytes.toString());
                        }
                        result.add(innerList);
                    }
                    response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, result);
                } else if (requestPathLower.equals("/base64urlmapofbytes")) {
                    final Map<String, String> result = new HashMap<>();
                    for (int i = 0; i < 2; ++i) {
                        final String key = Integer.toString(i);

                        final int byteCount = (i + 1) * 10;
                        final byte[] bytes = new byte[byteCount];
                        for (int j = 0; j < byteCount; ++j) {
                            bytes[j] = (byte) j;
                        }

                        final Base64Url base64UrlEncodedBytes = Base64Url.encode(bytes);
                        result.put(key, base64UrlEncodedBytes.toString());
                    }
                    response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, result);
                } else if (requestPathLower.equals("/datetimerfc1123")) {
                    final DateTimeRfc1123 now = new DateTimeRfc1123(OffsetDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC));
                    final String result = now.toString();
                    response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, result);
                } else if (requestPathLower.equals("/unixtime")) {
                    response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, 0);
                } else if (requestPathLower.equals("/delete")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.data(createHttpBinResponseDataForRequest(request));
                    response = new MockHttpResponse(request, 200, json);
                } else if (requestPathLower.equals("/get")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.headers(toMap(request.getHeaders()));
                    response = new MockHttpResponse(request, 200, json);
                } else if (requestPathLower.equals("/patch")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.data(createHttpBinResponseDataForRequest(request));
                    response = new MockHttpResponse(request, 200, json);
                } else if (requestPathLower.equals("/post")) {
                    if (contentType != null && contentType.contains("x-www-form-urlencoded")) {
                        Map<String, String> parsed = bodyToMap(request);
                        final HttpBinFormDataJSON json = new HttpBinFormDataJSON();
                        Form form = new Form();
                        form.customerName(parsed.get("custname"));
                        form.customerEmail(parsed.get("custemail"));
                        form.customerTelephone(parsed.get("custtel"));
                        form.pizzaSize(PizzaSize.valueOf(parsed.get("size")));
                        form.toppings(Arrays.asList(parsed.get("toppings").split(",")));
                        json.form(form);
                        response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, json);
                    } else {
                        final HttpBinJSON json = new HttpBinJSON();
                        json.url(request.getUrl().toString());
                        json.data(createHttpBinResponseDataForRequest(request));
                        json.headers(toMap(request.getHeaders()));
                        response = new MockHttpResponse(request, 200, json);
                    }
                } else if (requestPathLower.equals("/put")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.data(createHttpBinResponseDataForRequest(request));
                    json.headers(toMap(request.getHeaders()));
                    response = new MockHttpResponse(request, 200, RESPONSE_HEADERS, json);
                } else if (requestPathLower.startsWith("/status/")) {
                    final String statusCodeString = requestPathLower.substring("/status/".length());
                    final int statusCode = Integer.valueOf(statusCodeString);
                    response = new MockHttpResponse(request, statusCode);
                }
            } else if ("echo.org".equalsIgnoreCase(requestHost)) {
                return FluxUtil.collectBytesInByteBufferStream(request.getBody())
                    .map(bytes -> new MockHttpResponse(request, 200, new HttpHeaders(request.getHeaders()), bytes));
            }
        } catch (Exception ex) {
            return Mono.error(ex);
        }

        if (response == null) {
            response = new MockHttpResponse(request, 500);
        }

        return Mono.just(response);
    }

    private static String createHttpBinResponseDataForRequest(HttpRequest request) {
        String body = bodyToString(request);
        if (body == null) {
            return "";
        } else {
            return body;
        }
    }

    private static String bodyToString(HttpRequest request) {
        String body = "";
        if (request.getBody() != null) {
            Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(request.getBody())
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
            body = asyncString.block();
        }
        return body;
    }

    private static Map<String, String> toMap(HttpHeaders headers) {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers) {
            result.put(header.getName(), header.getValue());
        }
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
}
