// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Locale;

/**
 * This is a request policy in an {@link com.azure.core.http.HttpPipeline} to validate that metadata does not contain
 * leading or trailing whitespace characters.
 */
public class MetadataWhitespacePolicy implements HttpPipelinePolicy {

    private static ClientLogger logger = new ClientLogger(MetadataWhitespacePolicy.class);

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getHttpRequest().getHeaders().stream()
            .filter(header -> header.getName().toLowerCase(Locale.ROOT).startsWith("x-ms-"))
            .forEach(header -> {
                String name = header.getName().substring("x-ms-".length());
                boolean foundWhitespace = Character.isWhitespace(name.charAt(0))
                    || Character.isWhitespace(name.charAt(name.length() - 1));
                for (String value: header.getValues()) {
                    foundWhitespace |= Character.isWhitespace(value.charAt(0))
                        || Character.isWhitespace(value.charAt(value.length() - 1));
                }
                if (foundWhitespace) {
                    throw logger.logExceptionAsError(new IllegalArgumentException("Metadata keys and values can "
                        + "not contain leading or trailing whitespace. Please remove or encode them."));
                }
            });
        return next.process();
    }
}
