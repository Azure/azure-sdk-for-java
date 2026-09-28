// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * Rebuilds the request URL so the share/directory/file resource path and the route's query string are cleanly
 * separated, using the same {@link UrlBuilder#setPath(String)} mechanism the Queue Storage client uses to scope
 * requests to a resource path.
 * <p>
 * The share/directory/file resource is carried in the {@code @HostParam} URL while the generated routes are
 * query-only (for example {@code @Put("?restype=directory")}). When the host URL has a path, azure-core joins it to
 * the query-only route with a {@code '/'} separator, producing a {@code /?} sequence that the AutoRest client never
 * emitted (it modeled the resource as a {@code @PathParam}). The service tolerates the trailing slash for some
 * operations but rejects it for others, such as directory rename, with {@code InvalidResourceName}. Parsing the URL
 * and re-setting the path drops that trailing slash while preserving the query.
 * <p>
 * The policy is ordered before the credential policies in {@code BuilderHelper#buildPipeline} so the normalized path
 * is what SharedKey/SAS signs.
 */
public final class ResourceUrlNormalizationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ResourceUrlNormalizationPolicy.class);

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        scopeRequestToResourcePath(context.getHttpRequest());
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        scopeRequestToResourcePath(context.getHttpRequest());
        return next.processSync();
    }

    private static void scopeRequestToResourcePath(HttpRequest request) {
        UrlBuilder urlBuilder = UrlBuilder.parse(request.getUrl());
        String path = urlBuilder.getPath();
        // Only the spurious trailing '/' inserted before a query-only route needs to be removed; leave account-root
        // requests ("/" or empty path) untouched.
        if (path == null || path.length() <= 1 || path.charAt(path.length() - 1) != '/') {
            return;
        }
        urlBuilder.setPath(path.substring(0, path.length() - 1));
        try {
            request.setUrl(urlBuilder.toUrl());
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }
}
