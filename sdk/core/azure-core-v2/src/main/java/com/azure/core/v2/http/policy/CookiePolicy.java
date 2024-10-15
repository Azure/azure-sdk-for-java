// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http.policy;

import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>The {@code CookiePolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is
 * used to handle cookies in HTTP requests and responses.</p>
 *
 * <p>This class stores cookies from the "Set-Cookie" header of the HTTP response and adds them to subsequent HTTP
 * requests. This is useful for maintaining session information or other stateful information across multiple requests
 * to the same server.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code CookiePolicy} is constructed. The policy can then be added to a pipeline.
 * Any cookies set by the server in the response to a request by the pipeline will be stored by the {@code CookiePolicy}
 * and added to subsequent requests to the same server.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.CookiePolicy.constructor -->
 * <!-- end com.azure.core.http.policy.CookiePolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public class CookiePolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(CookiePolicy.class);
    private final CookieHandler cookies = new CookieManager();

    /**
     * Creates a new instance of {@link CookiePolicy}.
     */
    public CookiePolicy() {
    }

    @SuppressWarnings("deprecation")
    private static void beforeRequest(HttpRequest httpRequest, CookieHandler cookies) {
        try {
            final URI uri = httpRequest.getUri();

            Map<String, List<String>> cookieHeaders = new HashMap<>();
            for (HttpHeader header : httpRequest.getHeaders()) {
                cookieHeaders.put(String.valueOf(header.getName()), header.getValues());
            }

            Map<String, List<String>> requestCookies = cookies.get(uri, cookieHeaders);
            for (Map.Entry<String, List<String>> entry : requestCookies.entrySet()) {
                httpRequest.getHeaders().set(HttpHeaderName.fromString(entry.getKey()), entry.getValue());
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    private static Response<?> afterResponse(HttpRequest httpRequest, Response<?> response, CookieHandler cookies) {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        for (HttpHeader header : response.getHeaders()) {
            responseHeaders.put(String.valueOf(header.getName()), header.getValues());
        }
        try {
            final URI uri = httpRequest.getUri();
            cookies.put(uri, responseHeaders);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
        return response;
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        beforeRequest(httpRequest, cookies);

        return afterResponse(httpRequest, next.process(), cookies);
    }
}
