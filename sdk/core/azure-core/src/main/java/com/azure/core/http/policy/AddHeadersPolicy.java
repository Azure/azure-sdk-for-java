// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * <p>The {@code AddHeadersPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy
 * is used to add a set of headers to all outgoing HTTP requests.</p>
 *
 * <p>This class is useful when there are certain headers that should be included in all requests. For example, you
 * might want to include a "User-Agent" header in all requests to identify your application, or a "Content-Type" header
 * to specify the format of the request body.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, the {@code AddHeadersPolicy} is created from the specified headers. The policy can be added to
 * the pipeline and the requests sent will include the headers specified in the {@code AddHeadersPolicy}.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.AddHeaderPolicy.constructor -->
 * <pre>
 * HttpHeaders headers = new HttpHeaders&#40;&#41;;
 * headers.put&#40;&quot;User-Agent&quot;, &quot;MyApp&#47;1.0&quot;&#41;;
 * headers.put&#40;&quot;Content-Type&quot;, &quot;application&#47;json&quot;&#41;;
 *
 * new AddHeadersPolicy&#40;headers&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.AddHeaderPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 * @see com.azure.core.http.HttpHeaders
 */
public class AddHeadersPolicy implements HttpPipelinePolicy {
    private final HttpHeaders headers;

    /**
     * Creates a AddHeadersPolicy.
     *
     * @param headers The headers to add to outgoing requests.
     */
    public AddHeadersPolicy(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        setHeaders(context.getHttpRequest().getHeaders(), headers);

        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        setHeaders(context.getHttpRequest().getHeaders(), headers);

        return next.processSync();
    }

    private static void setHeaders(HttpHeaders requestHeaders, HttpHeaders policyHeaders) {
        requestHeaders.setAllHttpHeaders(policyHeaders);
    }
}
