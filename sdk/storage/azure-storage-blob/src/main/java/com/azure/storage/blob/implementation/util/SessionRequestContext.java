// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Carries the request-scoped parameters needed to obtain a {@link SessionCredential}, such as the target
 * container and account.
 * <p>
 * The session authentication policy populates the container name from the request URL. The account name is
 * resolved at client construction and carried by the acquirer. A nonempty container name and an acquirer are
 * required.
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
    private final SessionAcquirer acquirer;
    private final Context context;

    /**
     * Creates a new {@link SessionRequestContext}.
     *
     * @param containerName The container name. Must not be null or empty.
     * @param acquirer The session acquirer.
     * @param context The context of the request that triggered session acquisition.
     */
    SessionRequestContext(String containerName, SessionAcquirer acquirer, Context context) {
        if (CoreUtils.isNullOrEmpty(containerName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'containerName' cannot be null or empty."));
        }

        this.containerName = containerName;
        this.acquirer = Objects.requireNonNull(acquirer, "'acquirer' cannot be null.");
        this.context = Objects.requireNonNull(context, "'context' cannot be null.");
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
     * Gets the acquirer that sends CreateSession requests through the requesting client's pipeline.
     *
     * @return the acquirer.
     */
    SessionAcquirer getAcquirer() {
        return acquirer;
    }

    /**
     * Gets the context of the request that triggered session acquisition.
     *
     * @return the originating request context.
     */
    Context getContext() {
        return context;
    }
}
