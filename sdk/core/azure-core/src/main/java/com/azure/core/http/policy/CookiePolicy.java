// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The pipeline policy that which stores cookies based on the response "Set-Cookie" header and adds cookies to requests.
 */
public class CookiePolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(CookiePolicy.class);
    private final CookieHandler cookies = new CookieManager();

    /**
     * Creates a new instance of {@link CookiePolicy}.
     */
    public CookiePolicy() {
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.defer(() -> {
            beforeRequest(context.getHttpRequest(), cookies);
            return next.process();
        }).map(response -> afterResponse(context, response, cookies));
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        beforeRequest(context.getHttpRequest(), cookies);

        return afterResponse(context, next.processSync(), cookies);
    }

    @SuppressWarnings("deprecation")
    private static void beforeRequest(HttpRequest httpRequest, CookieHandler cookies) {
        try {
            final URI uri = httpRequest.getUrl().toURI();

            Map<String, List<String>> cookieHeaders = new HashMap<>();
            for (HttpHeader header : httpRequest.getHeaders()) {
                cookieHeaders.put(header.getName(), header.getValuesList());
            }

            Map<String, List<String>> requestCookies = cookies.get(uri, cookieHeaders);
            for (Map.Entry<String, List<String>> entry : requestCookies.entrySet()) {
                httpRequest.getHeaders().set(entry.getKey(), entry.getValue());
            }
        } catch (URISyntaxException | IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private static HttpResponse afterResponse(HttpPipelineCallContext context, HttpResponse response,
        CookieHandler cookies) {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        for (HttpHeader header : response.getHeaders()) {
            responseHeaders.put(header.getName(), header.getValuesList());
        }
        try {
            final URI uri = context.getHttpRequest().getUrl().toURI();
            cookies.put(uri, responseHeaders);
        } catch (URISyntaxException | IOException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
        }
        return response;
    }
}
