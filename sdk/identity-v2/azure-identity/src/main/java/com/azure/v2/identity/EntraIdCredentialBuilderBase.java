// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpRedirectOptions;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.traits.HttpTrait;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * <p>The base class for credential builders that allow specifying a client ID, tenant ID, authority host, and
 * additionally allowed tenants for Microsoft Entra ID.</p>
 *
 * @param <T> the type of the credential builder
 */
public abstract class EntraIdCredentialBuilderBase<T extends EntraIdCredentialBuilderBase<T>>
    extends CredentialBuilderBase<T> implements HttpTrait<T> {
    private static final ClientLogger LOGGER = new ClientLogger(EntraIdCredentialBuilderBase.class);

    /**
     * Constructs an instance of AadCredentialBuilderBase.
     */
    public EntraIdCredentialBuilderBase() {
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
        getClientOptions().setAuthorityHost(authorityHost);
        return (T) this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    @SuppressWarnings("unchecked")
    public T executorService(ExecutorService executorService) {
        getClientOptions().setExecutorService(executorService);
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
        getClientOptions().disableInstanceDiscovery();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy) {
        getHttpPipelineOptions().addHttpPipelinePolicy(pipelinePolicy);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T httpRetryOptions(HttpRetryOptions retryOptions) {
        getHttpPipelineOptions().setHttpRetryOptions(retryOptions);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T httpInstrumentationOptions(HttpInstrumentationOptions instrumentationOptions) {
        getHttpPipelineOptions().setHttpInstrumentationOptions(instrumentationOptions);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T httpRedirectOptions(HttpRedirectOptions redirectOptions) {
        getHttpPipelineOptions().setHttpRedirectOptions(redirectOptions);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T httpClient(HttpClient client) {
        getHttpPipelineOptions().setHttpClient(client);
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
        getClientOptions().setAdditionallyAllowedTenants(
            IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants)));
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
        getClientOptions()
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
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
        getClientOptions().setUnsafeSupportLoggingEnabled(true);
        return (T) this;
    }
}
