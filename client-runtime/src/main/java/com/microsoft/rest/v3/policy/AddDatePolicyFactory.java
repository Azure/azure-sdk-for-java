/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Creates a RequestPolicy which adds a Date header in RFC 1123 format when sending an HTTP request.
 */
public final class AddDatePolicyFactory implements RequestPolicyFactory {
    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new AddDatePolicy(next);
    }

    private static final class AddDatePolicy implements RequestPolicy {
        private final DateTimeFormatter format = DateTimeFormatter
                .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
                .withZone(ZoneId.of("UTC"))
                .withLocale(Locale.US);

        private final RequestPolicy next;

        AddDatePolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Mono<HttpResponse> sendAsync(HttpRequest request) {
            return Mono.defer(() -> {
                request.headers().set("Date", format.format(OffsetDateTime.now()));
                return next.sendAsync(request);
            });
        }
    }
}
