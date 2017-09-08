/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.google.common.io.CharStreams;
import com.microsoft.rest.v2.HttpBinJSON;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This HttpClient attempts to mimic the behavior of http://httpbin.org without ever making a network call.
 */
public class MockHttpClient extends HttpClient {
    @Override
    public Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
        HttpResponse response = new MockHttpResponse();

        try {
            final URI requestUrl = new URI(request.url());
            final String requestHost = requestUrl.getHost();
            if (requestHost.equalsIgnoreCase("httpbin.org")) {
                final String requestPath = requestUrl.getPath();
                if (requestPath.equalsIgnoreCase("/anything")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url();
                    json.headers = toMap(request.headers());
                    response = new MockHttpResponse(json);
                }
                else if (requestPath.startsWith("/bytes/")) {
                    final String byteCountString = requestPath.substring("/bytes/".length());
                    final int byteCount = Integer.parseInt(byteCountString);
                    response = new MockHttpResponse(new byte[byteCount]);
                }
                else if (requestPath.equalsIgnoreCase("/delete")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockHttpResponse(json);
                }
                else if (requestPath.equalsIgnoreCase("/get")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url();
                    json.headers = toMap(request.headers());
                    response = new MockHttpResponse(json);
                }
                else if (requestPath.equalsIgnoreCase("/patch")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockHttpResponse(json);
                }
                else if (requestPath.equalsIgnoreCase("/post")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockHttpResponse(json);
                }
                else if (requestPath.equalsIgnoreCase("/put")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockHttpResponse(json);
                }
            }
        }
        catch (Exception ignored) {
        }

        return Single.just(response);
    }

    private static String bodyToString(HttpRequest request) throws IOException {
        try (InputStream body = request.body()) {
            return CharStreams.toString(new InputStreamReader(body));
        }
    }

    private static Map<String, String> toMap(HttpHeaders headers) {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers) {
            result.put(header.name(), header.value());
        }
        return result;
    }
}
