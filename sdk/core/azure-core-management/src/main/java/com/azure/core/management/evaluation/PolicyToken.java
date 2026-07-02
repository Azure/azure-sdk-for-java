// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.evaluation;

import java.time.OffsetDateTime;

/**
 * <p>
 * Represents an immutable policy token acquired from the Azure Policy external evaluation ("Invoke") flow.
 * </p>
 *
 * <p>
 * A policy token is acquired by a {@link PolicyTokenCredential} when a resource operation is disallowed by policy
 * because an external evaluation policy token is missing. The acquired {@link #getToken() token} is applied verbatim
 * to the {@code x-ms-policy-external-evaluations} header on the retried resource operation.
 * </p>
 *
 * @see PolicyTokenCredential
 * @see PolicyTokenRequestContext
 */
public final class PolicyToken {
    private final String token;
    private final OffsetDateTime expiresAt;

    /**
     * Creates a policy token instance.
     *
     * @param token the verbatim value to apply to the {@code x-ms-policy-external-evaluations} header.
     */
    public PolicyToken(String token) {
        this(token, null);
    }

    /**
     * Creates a policy token instance.
     *
     * @param token the verbatim value to apply to the {@code x-ms-policy-external-evaluations} header.
     * @param expiresAt the time when the token expires, in UTC; may be {@code null} if not provided by the service.
     */
    public PolicyToken(String token, OffsetDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /**
     * Gets the policy token value.
     * <p>
     * This value is applied verbatim to the {@code x-ms-policy-external-evaluations} header on the retried resource
     * operation, including any scheme prefix (such as {@code "PoP "}) returned by the service.
     *
     * @return the policy token value.
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the time when the token expires, in UTC.
     *
     * @return the time when the token expires, in UTC; may be {@code null} if not provided by the service.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
}
