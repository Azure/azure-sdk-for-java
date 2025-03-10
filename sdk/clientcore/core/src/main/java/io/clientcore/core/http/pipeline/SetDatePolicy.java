// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.DateTimeRfc1123;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * <p>The {@code AddDatePolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is
 * used to add a "Date" header in RFC 1123 format when sending an HTTP request.</p>
 *
 * @see io.clientcore.core.http.pipeline
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public class SetDatePolicy implements HttpPipelinePolicy {
    private static final DateTimeFormatter FORMATTER
        = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneOffset.UTC).withLocale(Locale.US);

    /**
     * Creates a new instance of {@link SetDatePolicy}.
     */
    public SetDatePolicy() {
    }

    @Override
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        try {
            httpRequest.getHeaders().set(HttpHeaderName.DATE, DateTimeRfc1123.toRfc1123String(now));
        } catch (IllegalArgumentException ignored) {
            httpRequest.getHeaders().set(HttpHeaderName.DATE, FORMATTER.format(now));
        }
        return next.process();
    }

    @Override
    public final HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.AFTER_RETRY;
    }
}
