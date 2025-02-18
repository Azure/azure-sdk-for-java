// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;

import com.azure.identity.v2.implementation.util.IdentityUtil;
import com.azure.identity.v2.implementation.util.ValidationUtil;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpPipeline;
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
public abstract class EntraIdCredentialBuilderBase<T extends EntraIdCredentialBuilderBase<T>> extends CredentialBuilderBase<T> implements HttpTrait<T> {
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
        this.identityClientOptions.setAuthorityHost(authorityHost);
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
        this.identityClientOptions.setExecutorService(executorService);
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
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return An updated instance of this builder with the http pipeline set as specified.
     */
    @Override
    public T httpPipeline(HttpPipeline pipeline) {
        identityClientOptions.setHttpPipeline(pipeline);
        return (T) this;
    }

    @Override
    public T addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy) {
        this.identityClientOptions.addHttpPipelinePolicy(pipelinePolicy);
        return (T) this;
    }

    @Override
    public T httpRetryOptions(HttpRetryOptions retryOptions) {
        this.identityClientOptions.setHttpRetryOptions(retryOptions);
        return (T) this;
    }

    @Override
    public T httpInstrumentationOptions(HttpInstrumentationOptions instrumentationOptions) {
        this.identityClientOptions.setHttpInstrumentationOptions(instrumentationOptions);
        return (T) this;
    }

    @Override
    public T httpRedirectOptions(HttpRedirectOptions redirectOptions) {
        this.identityClientOptions.setHttpRedirectOptions(redirectOptions);
        return (T) this;
    }

    @Override
    public T httpClient(HttpClient client) {
        this.identityClientOptions.setHttpClient(client);
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
        identityClientOptions.setAdditionallyAllowedTenants(
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
        identityClientOptions
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return (T) this;
    }
}
