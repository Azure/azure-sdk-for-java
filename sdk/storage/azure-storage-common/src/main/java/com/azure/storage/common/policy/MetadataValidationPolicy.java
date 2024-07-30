// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

/**
 * This is a request policy in an {@link HttpPipeline} to validate that metadata does not contain leading or trailing
 * whitespace characters.
 * <p>
 * This is done as the service trims whitespace for the string to sign, but the client does not, resulting in an auth
 * failure.
 */
public class MetadataValidationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(MetadataValidationPolicy.class);
    // Header constant X_MS_META doesn't include the '-' in 'x-ms-meta-' (it is 'x-ms-meta')
    private static final String X_MS_META = Constants.HeaderConstants.X_MS_META + "-";
    private static final int X_MS_META_LENGTH = X_MS_META.length();

    /**
     * Creates a new instance of {@link MetadataValidationPolicy}.
     */
    public MetadataValidationPolicy() {
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        validateMetadataHeaders(context.getHttpRequest().getHeaders());
        return next.processSync();
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        try {
            validateMetadataHeaders(context.getHttpRequest().getHeaders());
        } catch (IllegalArgumentException ex) {
            return FluxUtil.monoError(LOGGER, ex);
        }

        return next.process();
    }

    static void validateMetadataHeaders(HttpHeaders headers) {
        for (HttpHeader header : headers) {
            String name = header.getName();

            // Using regionMatches is both faster with CPU and doesn't generate a temporary string
            // which toLowerCase(ROOT) does.
            if (!X_MS_META.regionMatches(true, 0, name, 0, X_MS_META_LENGTH)) {
                continue;
            }

            // First check if the name has whitespace.
            // Do not validate the name for being empty, that is left to the service to handle.
            boolean hasWhitespace = name.length() > X_MS_META_LENGTH && checkWhitespace(name, X_MS_META_LENGTH,
                name.length() - 1);

            // Then check if the value is not null or empty and has whitespace.
            String value = header.getValue();
            hasWhitespace |= hasWhitespace // boolean or against the existing check for an early out.
                || (!CoreUtils.isNullOrEmpty(value) && checkWhitespace(value, 0, value.length() - 1));

            if (hasWhitespace) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Metadata keys and values "
                    + "can not contain leading or trailing whitespace. Please remove or encode them."));
            }
        }
    }

    private static boolean checkWhitespace(String str, int leadIndex, int trailIndex) {
        return Character.isWhitespace(str.charAt(leadIndex)) || Character.isWhitespace(str.charAt(trailIndex));
    }
}
