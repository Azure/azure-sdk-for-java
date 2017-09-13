/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.http;

import com.google.common.io.CharStreams;
import com.microsoft.azure.v2.HttpBinJSON;
import com.microsoft.azure.v2.MockResource;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
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
public class MockAzureHttpClient extends HttpClient {
    @Override
    public Single<? extends HttpResponse> sendRequestInternalAsync(HttpRequest request) {
        HttpResponse response = new MockAzureHttpResponse();

        try {
            final URI requestUrl = new URI(request.url());
            final String requestHost = requestUrl.getHost();
            final String requestPath = requestUrl.getPath();
            final String requestPathLower = requestPath.toLowerCase();
            if (requestHost.equalsIgnoreCase("httpbin.org")) {
                if (requestPathLower.equals("/anything") || requestPathLower.startsWith("/anything/")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url()
                            // This is just to mimic the behavior we've seen with httpbin.org.
                            .replace("%20", " ");
                    json.headers = toMap(request.headers());
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.startsWith("/bytes/")) {
                    final String byteCountString = requestPath.substring("/bytes/".length());
                    final int byteCount = Integer.parseInt(byteCountString);
                    response = new MockAzureHttpResponse(200, new byte[byteCount]);
                }
                else if (requestPathLower.equals("/delete")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/get")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url();
                    json.headers = toMap(request.headers());
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/patch")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/post")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/put")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockAzureHttpResponse(200, json);
                }
            }
            else if (requestHost.equalsIgnoreCase("mock.azure.com")) {
                if (request.httpMethod().equalsIgnoreCase("GET")) {
                    if (requestPathLower.contains("/mockprovider/mockresources/")) {
                        final MockResource resource = new MockResource();
                        resource.name = requestPath.substring(requestPath.lastIndexOf('/') + 1);
                        response = new MockAzureHttpResponse(200, resource);
                    }
                    else if (requestPathLower.contains("/mockprovider/mockoperations/")) {
                        final Map<String,String> requestQueryMap = queryToMap(requestUrl.getQuery());

                        final String pollType = requestQueryMap.get("PollType");

                        final String pollsRemainingString = requestQueryMap.get("PollsRemaining");
                        int pollsRemaining;
                        try {
                            pollsRemaining = Integer.valueOf(pollsRemainingString);
                        }
                        catch (Exception ignored) {
                            pollsRemaining = 1;
                        }

                        if (pollsRemaining <= 1) {
                            final MockResource resource = new MockResource();
                            resource.name = "c";
                            response = new MockAzureHttpResponse(200, resource);
                        }
                        else {
                            --pollsRemaining;
                            final String locationUrl = "https://mock.azure.com/subscriptions/1/resourceGroups/mine/providers/mockprovider/mockoperations/1?PollType=" + pollType + "&PollsRemaining=" + pollsRemaining;
                            response = new MockAzureHttpResponse(202)
                                    .withHeader("Location", locationUrl);
                        }
                    }
                }
                else if (request.httpMethod().equalsIgnoreCase("PUT")) {
                    final Map<String,String> requestQueryMap = queryToMap(requestUrl.getQuery());

                    final String pollType = requestQueryMap.get("PollType");
                    String pollsRemaining = requestQueryMap.get("PollsRemaining");

                    if (pollType == null || "0".equals(pollsRemaining)) {
                        final MockResource resource = new MockResource();
                        resource.name = "c";
                        response = new MockAzureHttpResponse(200, resource);
                    }
                    else if (pollType.equals("Location")) {
                        if (pollsRemaining == null) {
                            pollsRemaining = "1";
                        }

                        final String locationUrl = "https://mock.azure.com/subscriptions/1/resourceGroups/mine/providers/mockprovider/mockoperations/1?PollType=Location&PollsRemaining=" + pollsRemaining;
                        response = new MockAzureHttpResponse(202)
                                .withHeader("Location", locationUrl);
                    }
                }
            }
        }
        catch (Exception ignored) {
        }

        return Single.just(response);
    }

    private static Map<String,String> queryToMap(String url) {
        final Map<String,String> result = new HashMap<>();

        if (url != null) {
            final int questionMarkIndex = url.indexOf('?');
            if (questionMarkIndex >= 0) {
                url = url.substring(questionMarkIndex + 1);
            }

            for (String querySegments : url.split("&")) {
                final String[] querySegmentParts = querySegments.split("=");
                result.put(querySegmentParts[0], querySegmentParts[1]);
            }
        }

        return result;
    }

    private static String bodyToString(HttpRequest request) throws IOException {
        try (final InputStream bodyStream = request.body().createInputStream()) {
            return CharStreams.toString(new InputStreamReader(bodyStream));
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
