// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.management.AsyncOperationResource;
import com.azure.core.test.implementation.entities.HttpBinJSON;
import com.azure.core.management.MockResource;
import com.azure.core.management.OperationState;
import com.azure.core.management.implementation.AzureAsyncOperationPollStrategy;
import com.azure.core.management.implementation.LocationPollStrategy;
import com.azure.core.test.http.MockHttpResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This HttpClient attempts to mimic the behavior of http://httpbin.org without ever making a network call.
 */
public class MockAzureHttpClient implements HttpClient {
    private int pollsRemaining;

    private int getRequests;
    private int createRequests;
    private int deleteRequests;
    private int pollRequests;

    public int getRequests() {
        return getRequests;
    }

    public int createRequests() {
        return createRequests;
    }

    public int deleteRequests() {
        return deleteRequests;
    }

    public int pollRequests() {
        return pollRequests;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        MockHttpResponse response = null;

        try {
            final URL requestUrl = request.getUrl();
            final String requestHost = requestUrl.getHost();
            final String requestPath = requestUrl.getPath();
            final String requestPathLower = requestPath.toLowerCase();
            if (requestHost.equalsIgnoreCase("httpbin.org")) {
                if (requestPathLower.equals("/anything") || requestPathLower.startsWith("/anything/")) {
                    if ("HEAD".equals(request.getHttpMethod())) {
                        response = new MockHttpResponse(request, 200, responseHeaders(), "");
                    } else {
                        final HttpBinJSON json = new HttpBinJSON();
                        json.url(request.getUrl().toString()
                                // This is just to mimic the behavior we've seen with httpbin.org.
                                .replace("%20", " "));
                        json.headers(toMap(request.getHeaders()));
                        response = new MockHttpResponse(request, 200, responseHeaders(), json);
                    }
                } else if (requestPathLower.startsWith("/bytes/")) {
                    final String byteCountString = requestPath.substring("/bytes/".length());
                    final int byteCount = Integer.parseInt(byteCountString);
                    response = new MockHttpResponse(request, 200, responseHeaders(), new byte[byteCount]);
                } else if (requestPathLower.equals("/delete")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.data(bodyToString(request));
                    response = new MockHttpResponse(request, 200, responseHeaders(), json);
                } else if (requestPathLower.equals("/get")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.headers(toMap(request.getHeaders()));
                    response = new MockHttpResponse(request, 200, responseHeaders(), json);
                } else if (requestPathLower.equals("/patch")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.data(bodyToString(request));
                    response = new MockHttpResponse(request, 200, responseHeaders(), json);
                } else if (requestPathLower.equals("/post")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.data(bodyToString(request));
                    response = new MockHttpResponse(request, 200, responseHeaders(), json);
                } else if (requestPathLower.equals("/put")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url(request.getUrl().toString());
                    json.data(bodyToString(request));
                    response = new MockHttpResponse(request, 200, responseHeaders(), json);
                } else if (requestPathLower.startsWith("/status/")) {
                    final String statusCodeString = requestPathLower.substring("/status/".length());
                    final int statusCode = Integer.valueOf(statusCodeString);
                    response = new MockHttpResponse(request, statusCode, responseHeaders(), new byte[0]);
                }
            } else if (requestHost.equalsIgnoreCase("mock.azure.com")) {
                if (request.getHttpMethod() == HttpMethod.GET) {
                    if (requestPathLower.contains("/mockprovider/mockresources/")) {
                        ++getRequests;
                        --pollsRemaining;

                        final MockResource resource = new MockResource();
                        resource.name(requestPath.substring(requestPath.lastIndexOf('/') + 1));
                        MockResource.Properties properties = new MockResource.Properties();
                        properties.provisioningState(pollsRemaining <= 0 ? OperationState.SUCCEEDED : OperationState.IN_PROGRESS);
                        resource.properties(properties);
                        response = new MockHttpResponse(request, 200, responseHeaders(), resource);
                    } else if (requestPathLower.contains("/mockprovider/mockoperations/")) {
                        ++pollRequests;

                        final Map<String, String> requestQueryMap = queryToMap(requestUrl.getQuery());

                        final String pollType = requestQueryMap.get("PollType");

                        if (pollType.equalsIgnoreCase(AzureAsyncOperationPollStrategy.HEADER_NAME)) {
                            String operationStatus;
                            if (pollsRemaining <= 1) {
                                operationStatus = OperationState.SUCCEEDED;
                            } else {
                                --pollsRemaining;
                                operationStatus = OperationState.IN_PROGRESS;
                            }
                            final AsyncOperationResource operationResource = new AsyncOperationResource();
                            operationResource.setStatus(operationStatus);
                            response = new MockHttpResponse(request, 200, responseHeaders(), operationResource);
                        } else if (pollType.equalsIgnoreCase(LocationPollStrategy.HEADER_NAME)) {
                            if (pollsRemaining <= 1) {
                                final MockResource mockResource = new MockResource();
                                mockResource.name("c");
                                MockResource.Properties properties = new MockResource.Properties();
                                properties.provisioningState(OperationState.SUCCEEDED);
                                mockResource.properties(properties);
                                response = new MockHttpResponse(request, 200, responseHeaders(), mockResource);
                            } else {
                                --pollsRemaining;
                                response = new MockHttpResponse(request, 202, responseHeaders(), new byte[0])
                                        .addHeader(LocationPollStrategy.HEADER_NAME, request.getUrl().toString());
                            }
                        }
                    }
                } else if (request.getHttpMethod() == HttpMethod.PUT) {
                    ++createRequests;

                    final Map<String, String> requestQueryMap = queryToMap(requestUrl.getQuery());

                    final String pollType = requestQueryMap.get("PollType");
                    String pollsRemainingString = requestQueryMap.get("PollsRemaining");

                    if (pollType == null || "0".equals(pollsRemainingString)) {
                        final MockResource resource = new MockResource();
                        resource.name("c");
                        MockResource.Properties properties = new MockResource.Properties();
                        properties.provisioningState(OperationState.SUCCEEDED);
                        resource.properties(properties);
                        response = new MockHttpResponse(request, 200, responseHeaders(), resource);
                    } else if (pollType.equalsIgnoreCase("ProvisioningState")) {

                        if (pollsRemainingString == null) {
                            pollsRemaining = 1;
                        } else {
                            pollsRemaining = Integer.valueOf(pollsRemainingString);
                        }

                        final MockResource resource = new MockResource();
                        resource.name("c");
                        resource.properties(new MockResource.Properties());
                        resource.properties().provisioningState((pollsRemaining <= 0 ? OperationState.SUCCEEDED : OperationState.IN_PROGRESS));
                        response = new MockHttpResponse(request, 200, responseHeaders(), resource);
                    } else {
                        if (pollsRemainingString == null) {
                            pollsRemaining = 1;
                        } else {
                            pollsRemaining = Integer.valueOf(pollsRemainingString);
                        }

                        final String initialResponseStatusCodeString = requestQueryMap.get("InitialResponseStatusCode");
                        int initialResponseStatusCode;
                        if (initialResponseStatusCodeString != null) {
                            initialResponseStatusCode = Integer.valueOf(initialResponseStatusCodeString);
                        } else if (pollType.equalsIgnoreCase(LocationPollStrategy.HEADER_NAME)) {
                            initialResponseStatusCode = 202;
                        } else {
                            initialResponseStatusCode = 201;
                        }

                        response = new MockHttpResponse(request, initialResponseStatusCode, responseHeaders(), new byte[0]);

                        final String pollUrl = "https://mock.azure.com/subscriptions/1/resourceGroups/mine/providers/mockprovider/mockoperations/1";
                        if (pollType.contains(AzureAsyncOperationPollStrategy.HEADER_NAME)) {
                            response.addHeader(AzureAsyncOperationPollStrategy.HEADER_NAME, pollUrl + "?PollType=" + AzureAsyncOperationPollStrategy.HEADER_NAME);
                        }
                        if (pollType.contains(LocationPollStrategy.HEADER_NAME)) {
                            response.addHeader(LocationPollStrategy.HEADER_NAME, pollUrl + "?PollType=" + LocationPollStrategy.HEADER_NAME);
                        }
                    }
                } else if (request.getHttpMethod() == HttpMethod.DELETE) {
                    ++deleteRequests;

                    final Map<String, String> requestQueryMap = queryToMap(requestUrl.getQuery());

                    final String pollType = requestQueryMap.get("PollType");
                    String pollsRemainingString = requestQueryMap.get("PollsRemaining");

                    if (pollType == null || "0".equals(pollsRemainingString)) {
                        response = new MockHttpResponse(request, 200, responseHeaders(), new byte[0]);
                    } else if (pollType.equals(LocationPollStrategy.HEADER_NAME)) {
                        if (pollsRemainingString == null) {
                            pollsRemaining = 1;
                        } else {
                            pollsRemaining = Integer.valueOf(pollsRemainingString);
                        }

                        final String initialResponseStatusCodeString = requestQueryMap.get("InitialResponseStatusCode");
                        int initialResponseStatusCode;
                        if (initialResponseStatusCodeString != null) {
                            initialResponseStatusCode = Integer.valueOf(initialResponseStatusCodeString);
                        } else if (pollType.equalsIgnoreCase(LocationPollStrategy.HEADER_NAME)) {
                            initialResponseStatusCode = 202;
                        } else {
                            initialResponseStatusCode = 201;
                        }

                        response = new MockHttpResponse(request, initialResponseStatusCode, responseHeaders(), new byte[0]);

                        final String pollUrl = "https://mock.azure.com/subscriptions/1/resourceGroups/mine/providers/mockprovider/mockoperations/1";
                        if (pollType.contains(AzureAsyncOperationPollStrategy.HEADER_NAME)) {
                            response.addHeader(AzureAsyncOperationPollStrategy.HEADER_NAME, pollUrl + "?PollType=" + AzureAsyncOperationPollStrategy.HEADER_NAME);
                        }
                        if (pollType.contains(LocationPollStrategy.HEADER_NAME)) {
                            response.addHeader(LocationPollStrategy.HEADER_NAME, pollUrl + "?PollType=" + LocationPollStrategy.HEADER_NAME);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return Mono.<HttpResponse>just(response);
    }

    private static Map<String, String> queryToMap(String url) {
        final Map<String, String> result = new HashMap<>();

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
        Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(request.getBody())
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
        return asyncString.block();
    }

    private static Map<String, String> toMap(HttpHeaders headers) {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers) {
            result.put(header.getName(), header.getValue());
        }
        return result;
    }

    public static HttpHeaders responseHeaders() {
        return new HttpHeaders()
                .put("Date", "Fri, 13 Oct 2017 20:33:09 GMT")
                .put("Via", "1.1 vegur")
                .put("Connection", "keep-alive")
                .put("X-Processed-Time", "1.0")
                .put("Access-Control-Allow-Credentials", "true")
                .put("Content-Type", "application/json");
    }
}
