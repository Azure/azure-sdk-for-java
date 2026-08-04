// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.implementation.util;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

/**
 * Restores the pre-TypeSpec (AutoRest) request URL shape for queue-scoped operations.
 * <p>
 * The TypeSpec-generated protocol layer carries the queue name in the client base URL and addresses queue-scoped
 * operations with query-only path templates (e.g. {@code @Get("?comp=metadata")}). azure-core's {@code RestProxy}
 * assembles those into {@code .../{queue}/?comp=metadata} -- a slash before the query -- whereas AutoRest emitted
 * {@code .../{queue}?comp=metadata} (the queue name was a path parameter). This policy strips that single trailing
 * path slash so the wire URL is identical to the AutoRest behavior, keeping the account-root slash for service-scoped
 * operations (path {@code "/"}) untouched. It must run before the credential policy so the shared-key signature is
 * computed over the corrected URL.
 */
public final class QueueUrlTrailingSlashPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(QueueUrlTrailingSlashPolicy.class);

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        normalizeUrl(context);
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        normalizeUrl(context);
        return next.processSync();
    }

    private static void normalizeUrl(HttpPipelineCallContext context) {
        UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        String path = urlBuilder.getPath();
        // Only strip a trailing slash on a non-root path (queue-scoped ops); leave the account-root "/" alone.
        if (path != null && path.length() > 1 && path.charAt(path.length() - 1) == '/') {
            urlBuilder.setPath(path.substring(0, path.length() - 1));
            try {
                context.getHttpRequest().setUrl(urlBuilder.toUrl());
            } catch (java.net.MalformedURLException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        }
    }
}
