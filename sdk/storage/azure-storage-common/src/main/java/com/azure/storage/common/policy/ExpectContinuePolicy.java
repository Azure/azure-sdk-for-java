// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelineSyncPolicy;
import com.azure.core.util.Configuration;

/**
 * Pipeline policy that applies the HTTP header {@code Expect: 100-continue} to requests that carry a body.
 * <p>
 * Must be placed after the retry policy so that it is evaluated on every retry attempt.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class ExpectContinuePolicy extends HttpPipelineSyncPolicy {
    private final long contentLengthThreshold;
    private final boolean disabled;

    /**
     * Creates a policy that applies {@code Expect: 100-continue} to every request with a body at least as large as the
     * given threshold.
     *
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header. A null value
     * means every request with a body is eligible.
     */
    public ExpectContinuePolicy(Long contentLengthThreshold) {
        this(contentLengthThreshold, Configuration.getGlobalConfiguration());
    }

    /**
     * Creates a policy reading the opt out from the given configuration.
     *
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header.
     * @param configuration The configuration to read the opt out from.
     */
    ExpectContinuePolicy(Long contentLengthThreshold, Configuration configuration) {
        this.contentLengthThreshold = contentLengthThreshold == null ? 0 : contentLengthThreshold;
        this.disabled = ExpectContinuePolicyHelper.isDisabled(configuration);
    }

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        HttpRequest request = context.getHttpRequest();
        if (!disabled && ExpectContinuePolicyHelper.isEligible(request, contentLengthThreshold)) {
            ExpectContinuePolicyHelper.applyHeader(request);
        }
    }
}
