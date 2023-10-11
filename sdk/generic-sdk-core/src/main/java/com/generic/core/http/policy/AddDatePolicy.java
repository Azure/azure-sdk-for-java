// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.HttpHeaderName;
import com.generic.core.http.HttpPipelineCallContext;
import com.generic.core.http.HttpPipelineNextSyncPolicy;
import com.generic.core.http.HttpResponse;
import com.generic.core.util.DateTimeRfc1123;

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
