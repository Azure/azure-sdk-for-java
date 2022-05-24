// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
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

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.defer(() -> {
            extracted(context);
            return next.process();
        });
    }
    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        extracted(context);
        return next.processSync();
    }

    private void extracted(HttpPipelineCallContext context) {
        OffsetDateTime now = OffsetDateTime.now();
        try {
            context.getHttpRequest().getHeaders().set("Date", DateTimeRfc1123.toRfc1123String(now));
        } catch (IllegalArgumentException ignored) {
            context.getHttpRequest().getHeaders().set("Date", FORMATTER.format(now));
        }
    }
}
