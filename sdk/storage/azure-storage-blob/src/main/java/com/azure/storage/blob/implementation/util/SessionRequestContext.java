// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.options.SessionOptions;

import java.util.Objects;

/**
 * Carries the request-scoped parameters needed to obtain a {@link SessionCredential}, such as the target
 * container and account.
 * <p>
 * The session authentication policy populates the container name from the request URL and the account name
 * from {@link SessionOptions#getAccountName()} or the request URL. This object does not resolve missing
 * values itself. When calling the built-in provider directly, a nonempty container name and an acquirer are
 * required, and an account name must be supplied either by this context or by the acquirer.
 * <p>
 * This exists so a single provider instance can be asked for a session that is scoped to a
 * specific container at call time, rather than being permanently bound to one container at construction
 * time - allowing one provider to serve sessions for many containers.
 *
 * RESERVED FOR INTERNAL USE.
 */
final class SessionRequestContext {
    private static final ClientLogger LOGGER = new ClientLogger(SessionRequestContext.class);

    private final String containerName;
    private final String accountName;
    private final SessionAcquirer acquirer;

    /**
     * Creates a new {@link SessionRequestContext}.
     *
     * @param containerName The container name. Must not be null or empty.
     * @param accountName The account name, or {@code null} if it should be resolved from the acquirer.
     * @param acquirer The session acquirer.
     */
    SessionRequestContext(String containerName, String accountName, SessionAcquirer acquirer) {
        if (CoreUtils.isNullOrEmpty(containerName)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'containerName' cannot be null or empty."));
        }

        this.containerName = containerName;
        this.accountName = accountName;
        this.acquirer = Objects.requireNonNull(acquirer, "'acquirer' cannot be null.");
    }

    /**
     * Gets the name of the container the session should be scoped to.
     *
     * @return the container name.
     */
    String getContainerName() {
        return containerName;
    }

    /**
     * Gets the name of the storage account the session should be scoped to, if known.
     *
     * @return the account name, or {@code null} if not resolved/known for this request.
     */
    String getAccountName() {
        return accountName;
    }

    /**
     * Gets the acquirer that sends CreateSession requests through the requesting client's pipeline.
     *
     * @return the acquirer.
     */
    SessionAcquirer getAcquirer() {
        return acquirer;
    }
}
