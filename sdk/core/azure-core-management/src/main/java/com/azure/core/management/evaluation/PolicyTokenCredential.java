// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.evaluation;

import reactor.core.publisher.Mono;

/**
 * <p>
 * The {@link PolicyTokenCredential} interface serves as the component responsible for acquiring a {@link PolicyToken}
 * required by the Azure Policy external evaluation ("Invoke") flow.
 * </p>
 *
 * <p>
 * When a resource operation is disallowed by policy because an external evaluation policy token is missing, the
 * {@link com.azure.core.management.http.policy.ExternalEvaluationPolicy} calls
 * {@link #getPolicyToken(PolicyTokenRequestContext)} (or its synchronous counterpart) to acquire a policy token, and
 * then retries the resource operation with the acquired token applied to the
 * {@code x-ms-policy-external-evaluations} header.
 * </p>
 *
 * <p>
 * Implementations of this interface encapsulate the call to the {@code acquirePolicyToken} REST operation, keeping the
 * request and response wire contract out of {@code azure-core-management}. An implementation is expected to complete
 * with an error when the service does not issue a token (for example, when the external evaluation result is a deny).
 * </p>
 *
 * @see PolicyToken
 * @see PolicyTokenRequestContext
 */
@FunctionalInterface
public interface PolicyTokenCredential {
    /**
     * Asynchronously acquires a {@link PolicyToken} for the given resource operation.
     *
     * @param request the details of the resource operation that requires a policy token.
     * @return a {@link Mono} that emits a single {@link PolicyToken}, or completes with an error when a token cannot
     * be acquired.
     */
    Mono<PolicyToken> getPolicyToken(PolicyTokenRequestContext request);

    /**
     * Synchronously acquires a {@link PolicyToken} for the given resource operation.
     *
     * @param request the details of the resource operation that requires a policy token.
     * @return the acquired {@link PolicyToken}.
     */
    default PolicyToken getPolicyTokenSync(PolicyTokenRequestContext request) {
        return getPolicyToken(request).block();
    }
}
