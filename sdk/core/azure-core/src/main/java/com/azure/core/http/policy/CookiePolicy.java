// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;

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
public class CookiePolicy extends HttpPipelineSynchronousPolicy {
    private final ClientLogger logger = new ClientLogger(CookiePolicy.class);
    private final CookieHandler cookies = new CookieManager();

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        try {
            final HttpRequest httpRequest = context.getHttpRequest();
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
            // TODO (kasobol-msft) should we wrap or add throws and handle in base?
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    protected HttpResponse afterReceivedResponse(HttpPipelineCallContext context, HttpResponse response) {
        try {
            final HttpRequest httpRequest = context.getHttpRequest();
            final URI uri = httpRequest.getUrl().toURI();

            Map<String, List<String>> responseHeaders = new HashMap<>();
            for (HttpHeader header : response.getHeaders()) {
                responseHeaders.put(header.getName(), header.getValuesList());
            }

            cookies.put(uri, responseHeaders);
            return response;
        } catch (URISyntaxException | IOException e) {
            // TODO (kasobol-msft) should we wrap or add throws and handle in base?
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }
}
