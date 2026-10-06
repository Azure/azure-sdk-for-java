// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Carries the request-scoped parameters needed to obtain a {@link SessionCredential}, such as the target
 * container and account.
 * <p>
 * The session authentication policy populates the container name from the request URL and the account name
 * from {@link SessionOptions#getAccountName()} or the request URL. This object does not resolve missing
 * values itself. When calling the built-in provider directly, a nonempty container name is required, and
 * an account name must be supplied either by the provider's configuration or by this context.
 * Custom providers define their own requirements for these values.
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
