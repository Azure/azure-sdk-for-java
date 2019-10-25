// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The pipeline policy that which stores cookies based on the response "Set-Cookie" header and adds cookies to requests.
 */
public class CookiePolicy implements HttpPipelinePolicy {
    private final ClientLogger logger = new ClientLogger(CookiePolicy.class);
    private final CookieHandler cookies = new CookieManager();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        try {
            final URI uri = context.getHttpRequest().getUrl().toURI();

            Map<String, List<String>> cookieHeaders = new HashMap<>();
            for (HttpHeader header : context.getHttpRequest().getHeaders()) {
                cookieHeaders.put(header.getName(), Arrays.asList(context.getHttpRequest().getHeaders()
                    .getValues(header.getName())));
            }

            Map<String, List<String>> requestCookies = cookies.get(uri, cookieHeaders);
            for (Map.Entry<String, List<String>> entry : requestCookies.entrySet()) {
                context.getHttpRequest().getHeaders().put(entry.getKey(), String.join(",", entry.getValue()));
            }

            return next.process().map(httpResponse -> {
                Map<String, List<String>> responseHeaders = new HashMap<>();
                for (HttpHeader header : httpResponse.getHeaders()) {
                    responseHeaders.put(header.getName(), Collections.singletonList(header.getValue()));
                }

                try {
                    cookies.put(uri, responseHeaders);
                } catch (IOException e) {
                    throw logger.logExceptionAsError(Exceptions.propagate(e));
                }
                return httpResponse;
            });
        } catch (URISyntaxException | IOException e) {
            return Mono.error(e);
        }
    }
}
