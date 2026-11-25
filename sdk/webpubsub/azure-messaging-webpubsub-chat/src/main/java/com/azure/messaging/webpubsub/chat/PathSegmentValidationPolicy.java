// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.policy.HttpPipelineSyncPolicy;
import com.azure.core.util.logging.ClientLogger;

import java.util.regex.Pattern;

final class PathSegmentValidationPolicy extends HttpPipelineSyncPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(PathSegmentValidationPolicy.class);
    // REST path substitution escapes '%' in already encoded input to '%25'.
    private static final Pattern DOT_SEGMENT
        = Pattern.compile("/(?:\\.|%(?:25)*2e){1,2}(?:/|$)", Pattern.CASE_INSENSITIVE);

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        if (DOT_SEGMENT.matcher(context.getHttpRequest().getUrl().getPath()).find()) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("URL path segments must not be '.' or '..'."));
        }
    }
}
