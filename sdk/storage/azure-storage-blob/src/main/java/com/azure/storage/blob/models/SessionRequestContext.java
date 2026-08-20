// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Carries the request-scoped parameters needed to obtain a {@link SessionCredential}, such as the target
 * container and account.
 * <p>
 * Both {@code containerName} and {@code accountName} are optional: they are resolved automatically from the
 * request URL in the common case, and are only needed here when that automatic resolution isn't possible or
 * isn't correct - for example, a custom domain URL that a {@link SessionProvider} implementation cannot
 * parse the account name from, or a proxying scenario where the effective container differs from the one on
 * the wire. A {@link SessionProvider} implementation should treat either value as a hint that may be absent
 * rather than something it can always rely on.
 * <p>
 * This exists so a single {@link SessionProvider} instance can be asked for a session that is scoped to a
 * specific container at call time, rather than being permanently bound to one container at construction
 * time - allowing one provider to serve sessions for many containers.
 *
 * @see SessionProvider
 */
public final class SessionRequestContext {

    private String containerName;
    private String accountName;

    /**
     * Creates a new {@link SessionRequestContext}.
     */
    public SessionRequestContext() {
    }

    /**
     * Gets the name of the container the session should be scoped to, if known.
     *
     * @return the container name, or {@code null} if not resolved/known for this request.
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Sets the name of the container the session should be scoped to.
     *
     * @param containerName the container name.
     * @return the updated {@link SessionRequestContext} object.
     */
    public SessionRequestContext setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Gets the name of the storage account the session should be scoped to, if known.
     *
     * @return the account name, or {@code null} if not resolved/known for this request.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the name of the storage account the session should be scoped to.
     *
     * @param accountName the account name.
     * @return the updated {@link SessionRequestContext} object.
     */
    public SessionRequestContext setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }
}
