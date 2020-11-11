// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;

import java.util.List;

/**
 * The base interface for allowing configurations to be made on the HTTP client.
 *
 * @param <T> the actual type of the interface extending this interface
 */
public interface AzureConfigurable<T extends AzureConfigurable<T>> {

    /**
     * Set the logging options on the HTTP client.
     *
     * @param logOptions the HttpLogDetailLevel logging options
     * @return the configurable object itself
     */
    T withLogOptions(HttpLogOptions logOptions);

    /**
     * Sets the logging detail level on the HTTP client.
     *
     * If set, this configure will override {@link HttpLogOptions#setLogLevel(HttpLogDetailLevel)} configure of
     * {@link AzureConfigurable#withLogOptions(HttpLogOptions)}.

     * @param logLevel the logging level
     * @return the configurable object itself
     */
    T withLogLevel(HttpLogDetailLevel logLevel);

    /**
     * Plug in a policy into the HTTP pipeline.
     *
     * @param policy the policy to plug in
     * @return the configurable object itself
     */
    T withPolicy(HttpPipelinePolicy policy);

    /**
     * Set the cross-tenant auxiliary credentials for Azure which can hold up to three.
     *
     * @param token the auxiliary credential
     * @return the configurable object itself
     */
    T withAuxiliaryCredential(TokenCredential token);

    /**
     * Set the cross-tenant auxiliary credentials for Azure which can hold up to three.
     *
     * @param tokens the auxiliary credentials
     * @return the configurable object itself
     */
    T withAuxiliaryCredentials(List<TokenCredential> tokens);

    /**
     * Sets the retry policy used in http pipeline.
     *
     * @param retryPolicy the retry policy
     * @return the configurable object itself for chaining
     */
    T withRetryPolicy(RetryPolicy retryPolicy);

    /**
     * Sets the credential scope.
     *
     * @param scope the credential scope
     * @return the configurable object itself for chaining
     */
    T withScope(String scope);

    /**
     * Sets the credential scopes.
     *
     * @param scopes the credential scope
     * @return the configurable object itself for chaining
     */
    T withScopes(List<String> scopes);

    /**
     * Sets the http client.
     *
     * @param httpClient the http client
     * @return the configurable object itself for chaining
     */
    T withHttpClient(HttpClient httpClient);

    /**
     * Sets the configuration.
     *
     * @param configuration the proxy to use
     * @return the configurable object itself for chaining
     */
    T withConfiguration(Configuration configuration);
}
