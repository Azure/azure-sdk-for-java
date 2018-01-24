/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.google.common.base.Charsets;
import com.microsoft.rest.v2.Base64Url;
import com.microsoft.rest.v2.DateTimeRfc1123;
import com.microsoft.rest.v2.entities.HttpBinJSON;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.functions.Function;
import org.joda.time.DateTime;
import io.reactivex.Single;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This HttpClient attempts to mimic the behavior of http://httpbin.org without ever making a network call.
 */
public class MockHttpClient extends HttpClient {
    private static final HttpHeaders responseHeaders = new HttpHeaders()
            .set("Date", "Fri, 13 Oct 2017 20:33:09 GMT")
            .set("Via", "1.1 vegur")
            .set("Connection", "keep-alive")
            .set("X-Processed-Time", "1.0")
            .set("Access-Control-Allow-Credentials", "true")
            .set("Content-Type", "application/json");

    @Override
    public Single<HttpResponse> sendRequestAsync(HttpRequest request) {
        HttpResponse response = null;

        try {
            final URL requestUrl = request.url();
            final String requestHost = requestUrl.getHost();
            if ("httpbin.org".equalsIgnoreCase(requestHost)) {
                final String requestPath = requestUrl.getPath();
                final String requestPathLower = requestPath.toLowerCase();
                if (requestPathLower.equals("/anything") || requestPathLower.startsWith("/anything/")) {
                    if ("HEAD".equals(request.httpMethod())) {
                        response = new MockHttpResponse(200, "");
                    } else {
                        final HttpBinJSON json = new HttpBinJSON();
                        json.url = request.url().toString()
                                // This is just to mimic the behavior we've seen with httpbin.org.
                                .replace("%20", " ");
                        json.headers = toMap(request.headers());
                        response = new MockHttpResponse(200, json);
                    }
                }
                else if (requestPathLower.startsWith("/bytes/")) {
                    final String byteCountString = requestPath.substring("/bytes/".length());
                    final int byteCount = Integer.parseInt(byteCountString);
                    HttpHeaders newHeaders = new HttpHeaders(responseHeaders)
                            .set("Content-Type", "application/octet-stream")
                            .set("Content-Length", Integer.toString(byteCount));
                    response = new MockHttpResponse(200, newHeaders, new byte[byteCount]);
                }
                else if (requestPathLower.startsWith("/base64urlbytes/")) {
                    final String byteCountString = requestPath.substring("/base64urlbytes/".length());
                    final int byteCount = Integer.parseInt(byteCountString);
                    final byte[] bytes = new byte[byteCount];
                    for (int i = 0; i < byteCount; ++i) {
                        bytes[i] = (byte)i;
                    }
                    final Base64Url base64EncodedBytes = Base64Url.encode(bytes);
                    response = new MockHttpResponse(200, responseHeaders, base64EncodedBytes);
                }
                else if (requestPathLower.equals("/base64urllistofbytes")) {
                    final List<String> base64EncodedBytesList = new ArrayList<>();
                    for (int i = 0; i < 3; ++i) {
                        final int byteCount = (i + 1) * 10;
                        final byte[] bytes = new byte[byteCount];
                        for (int j = 0; j < byteCount; ++j) {
                            bytes[j] = (byte)j;
                        }
                        final Base64Url base64UrlEncodedBytes = Base64Url.encode(bytes);
                        base64EncodedBytesList.add(base64UrlEncodedBytes.toString());
                    }
                    response = new MockHttpResponse(200, responseHeaders, base64EncodedBytesList);
                }
                else if (requestPathLower.equals("/base64urllistoflistofbytes")) {
                    final List<List<String>> result = new ArrayList<>();
                    for (int i = 0; i < 2; ++i) {
                        final List<String> innerList = new ArrayList<>();
                        for (int j = 0; j < (i + 1) * 2; ++j) {
                            final int byteCount = (j + 1) * 5;
                            final byte[] bytes = new byte[byteCount];
                            for (int k = 0; k < byteCount; ++k) {
                                bytes[k] = (byte)k;
                            }

                            final Base64Url base64UrlEncodedBytes = Base64Url.encode(bytes);
                            innerList.add(base64UrlEncodedBytes.toString());
                        }
                        result.add(innerList);
                    }
                    response = new MockHttpResponse(200, responseHeaders, result);
                }
                else if (requestPathLower.equals("/base64urlmapofbytes")) {
                    final Map<String,String> result = new HashMap<>();
                    for (int i = 0; i < 2; ++i) {
                        final String key = Integer.toString(i);

                        final int byteCount = (i + 1) * 10;
                        final byte[] bytes = new byte[byteCount];
                        for (int j = 0; j < byteCount; ++j) {
                            bytes[j] = (byte)j;
                        }

                        final Base64Url base64UrlEncodedBytes = Base64Url.encode(bytes);
                        result.put(key, base64UrlEncodedBytes.toString());
                    }
                    response = new MockHttpResponse(200, responseHeaders, result);
                }
                else if (requestPathLower.equals("/datetimerfc1123")) {
                    final DateTimeRfc1123 now = new DateTimeRfc1123(new DateTime(0));
                    final String result = now.toString();
                    response = new MockHttpResponse(200, responseHeaders, result);
                }
                else if (requestPathLower.equals("/unixtime")) {
                    response = new MockHttpResponse(200, responseHeaders, 0);
                }
                else if (requestPathLower.equals("/delete")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url().toString();
                    json.data = bodyToString(request);
                    response = new MockHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/get")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url().toString();
                    json.headers = toMap(request.headers());
                    response = new MockHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/patch")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url().toString();
                    json.data = bodyToString(request);
                    response = new MockHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/post")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url().toString();
                    json.data = bodyToString(request);
                    response = new MockHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/put")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url().toString();
                    json.data = bodyToString(request);
                    response = new MockHttpResponse(200, responseHeaders, json);
                }
                else if (requestPathLower.startsWith("/status/")) {
                    final String statusCodeString = requestPathLower.substring("/status/".length());
                    final int statusCode = Integer.valueOf(statusCodeString);
                    response = new MockHttpResponse(statusCode);
                }
            }
        }
        catch (Exception ex) {
            return Single.error(ex);
        }

        if (response == null) {
            response = new MockHttpResponse(500);
        }

        return Single.just(response);
    }

    private static String bodyToString(HttpRequest request) throws IOException {
        String body = "";
        if (request.body() != null) {
            Single<String> asyncString = FlowableUtil.collectBytes(request.body())
                    .map(new Function<byte[], String>() {
                @Override
                public String apply(byte[] bytes) throws Exception {
                    return new String(bytes, Charsets.UTF_8);
                }
            });
            body = asyncString.blockingGet();
        }

        return body;
    }

    private static Map<String, String> toMap(HttpHeaders headers) {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers) {
            result.put(header.name(), header.value());
        }
        return result;
    }
}
