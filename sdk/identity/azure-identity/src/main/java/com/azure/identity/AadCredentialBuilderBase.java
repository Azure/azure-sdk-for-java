// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * <p>The base class for credential builders that allow specifying a client ID, tenant ID, authority host, and
 * additionally allowed tenants for Microsoft Entra ID.</p>
 *
 * @param <T> the type of the credential builder
 */
public abstract class AadCredentialBuilderBase<T extends AadCredentialBuilderBase<T>> extends CredentialBuilderBase<T> {
    private static final ClientLogger LOGGER = new ClientLogger(AadCredentialBuilderBase.class);

    String clientId;
    String tenantId;

    /**
     * Constructs an instance of AadCredentialBuilderBase.
     */
    public AadCredentialBuilderBase() {
        super();
    }

    /**
     * Specifies the Microsoft Entra endpoint to acquire tokens.
     * @param authorityHost the Microsoft Entra endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    @SuppressWarnings("unchecked")
    public T authorityHost(String authorityHost) {
        ValidationUtil.validateAuthHost(authorityHost, LOGGER);
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
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        this.tenantId = tenantId;
        return (T) this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link ForkJoinPool#commonPool() common fork join pool} will be used which is
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
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant on which the application is installed.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    @SuppressWarnings("unchecked")
    public T additionallyAllowedTenants(String... additionallyAllowedTenants) {
        identityClientOptions
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants)));
        return (T) this;
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant on which the application is installed.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    @SuppressWarnings("unchecked")
    public T additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return (T) this;
    }

    /**
     * Disables the setting which determines whether or not instance discovery is performed when attempting to
     * authenticate. This will completely disable both instance discovery and authority validation.
     * This functionality is intended for use in scenarios where the metadata endpoint cannot be reached, such as in
     * private clouds or Azure Stack. The process of instance discovery entails retrieving authority metadata from
     * https://login.microsoft.com/ to validate the authority. By utilizing this API, the validation of the authority
     * is disabled. As a result, it is crucial to ensure that the configured authority host is valid and trustworthy.
     *
     * @return An updated instance of this builder with instance discovery disabled.
     */
    @SuppressWarnings("unchecked")

    public T disableInstanceDiscovery() {
        this.identityClientOptions.disableInstanceDiscovery();
        return (T) this;
    }

    /**
     * Enables additional support logging for public and confidential client applications. This enables
     * PII logging in MSAL4J as described <a href="https://learn.microsoft.com/entra/msal/java/advanced/msal-logging-java#personal-and-organization-information">here.</a>
     *
     * <p><b>This operation will log PII including tokens. It should only be used when directed by support.</b>
     *
     * @return An updated instance of this builder with additional support logging enabled.
     */
    @SuppressWarnings("unchecked")
    public T enableUnsafeSupportLogging() {
        this.identityClientOptions.enableUnsafeSupportLogging();
        return (T) this;
    }
}
