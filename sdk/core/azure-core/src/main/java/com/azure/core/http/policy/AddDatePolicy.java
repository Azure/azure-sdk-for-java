// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.DateTimeRfc1123;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The pipeline policy that adds a "Date" header in RFC 1123 format when sending an HTTP request.
 */
public class AddDatePolicy implements HttpPipelinePolicy {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withZone(ZoneOffset.UTC)
            .withLocale(Locale.US);

    /**
     * Creates a new instance of {@link AddDatePolicy}.
     */
    public AddDatePolicy() {
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.defer(() -> {
            setDate(context);
            return next.process();
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        setDate(context);
        return next.processSync();
    }

    private static void setDate(HttpPipelineCallContext context) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        try {
            context.getHttpRequest().setHeader(HttpHeaderName.DATE, DateTimeRfc1123.toRfc1123String(now));
        } catch (IllegalArgumentException ignored) {
            context.getHttpRequest().setHeader(HttpHeaderName.DATE, FORMATTER.format(now));
        }
    }
}
