// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

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
        ValidationUtil.validateAuthHost(getClass().getSimpleName(), authorityHost);
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
        ValidationUtil.validateTenantIdCharacterRange(getClass().getSimpleName(), tenantId);
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
}
