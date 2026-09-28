// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * Removes the {@code '/'} that azure-core inserts before a query-only route when the share/directory/file resource is
 * carried in the {@code @HostParam} URL. This is a TypeSpec-migration artifact (the AutoRest client modeled the
 * resource as a {@code @PathParam}, so it never occurred); some operations, such as directory rename, are rejected by
 * the service with {@code InvalidResourceName} when the request URL has this {@code /?} sequence.
 */
public final class ResourceUrlNormalizationPolicy implements HttpPipelinePolicy {
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        normalize(context.getHttpRequest());
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        normalize(context.getHttpRequest());
        return next.processSync();
    }

    private static void normalize(HttpRequest request) {
        String url = request.getUrl().toString();
        int queryIndex = url.indexOf('?');
        int boundary = queryIndex == -1 ? url.length() : queryIndex;
        if (boundary > 0 && url.charAt(boundary - 1) == '/') {
            request.setUrl(url.substring(0, boundary - 1) + url.substring(boundary));
        }
    }
}
