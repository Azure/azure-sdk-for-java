// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The pipeline policy that adds a "Date" header in RFC 1123 format when sending an HTTP request.
 */
public class AddDatePolicy implements HttpPipelinePolicy {
    private final DateTimeFormatter format = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withZone(ZoneId.of("UTC"))
            .withLocale(Locale.US);

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.defer(() -> {
            context.getHttpRequest().getHeaders().set("Date", format.format(OffsetDateTime.now()));
            return next.process();
        });
    }
}
