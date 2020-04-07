// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * The base class for credential builders that allow specifying a client ID and tenant ID for an Azure Active Directory.
 * @param <T> the type of the credential builder
 */
public abstract class AadCredentialBuilderBase<T extends AadCredentialBuilderBase<T>> extends CredentialBuilderBase<T> {
    String clientId;
    String tenantId;

    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    @SuppressWarnings("unchecked")
    public T authorityHost(String authorityHost) {
        this.identityClientOptions.setAuthorityHost(authorityHost);
        return (T) this;
    }

    /**
     * Sets the client ID of the application.
     *
     * @param clientId the client ID of the application.
     * @return An updated instance of this builder with the client id set as specified.
     */
    @SuppressWarnings("unchecked")
    public T clientId(String clientId) {
        this.clientId = clientId;
        return (T) this;
    }

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public T tenantId(String tenantId) {
        this.tenantId = tenantId;
        return (T) this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link ForkJoinPool#commonPool()} will be used which is
     * also shared with other application tasks. If the common pool is heavily used for other tasks, authentication
     * requests might starve and setting up this executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    @SuppressWarnings("unchecked")
    public T executorService(ExecutorService executorService) {
        this.identityClientOptions.setExecutorService(executorService);
        return (T) this;
    }

    /**
     * Sets whether to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is false by default.
     *
     * @param useUnprotectedFileOnLinux whether to use an unprotected file for cache storage.
     *
     * @return The updated T object.
     */
    @SuppressWarnings("unchecked")
    public T useUnprotectedTokenCacheFileOnLinux(boolean useUnprotectedFileOnLinux) {
        this.identityClientOptions.setUseUnprotectedTokenCacheFileOnLinux(useUnprotectedFileOnLinux);
        return (T) this;
    }

    /**
     * Disable using the shared token cache.
     *
     * @param disabled whether to disable using the shared token cache.
     *
     * @return The updated identity client options.
     */
    @SuppressWarnings("unchecked")
    public T disableSharedTokenCache(boolean disabled) {
        this.identityClientOptions.disableSharedTokenCache(disabled);
        return (T) this;
    }
}
